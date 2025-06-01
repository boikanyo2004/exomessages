<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

$servername = "localhost";
$username = "s2543085";
$password = "s2543085";
$dbname = "d2543085";


try {
    $pdo = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    
    $input = json_decode(file_get_contents('php://input'), true);
    $userId = isset($input['userId']) ? (int)$input['userId'] : 0;
    
    if ($userId <= 0) {
        echo json_encode([
            'status' => 'error',
            'message' => 'Invalid user ID'
        ]);
        exit;
    }
    
       $stmt = $pdo->prepare("
        SELECT 
            s.statusId,
            s.statusText,
            s.createdAt,
            COUNT(u.upvoteId) as upvotes,
            GROUP_CONCAT(us.username SEPARATOR ', ') as friendsUpvoted
        FROM statuses s
        LEFT JOIN STATUS_UPVOTES u ON s.statusId = u.statusId
        LEFT JOIN USER us ON u.userId = us.userId AND us.userId != s.userId
        WHERE s.userId = ?
        GROUP BY s.statusId, s.statusText, s.createdAt
        ORDER BY s.createdAt DESC
    ");
    
    $stmt->execute([$userId]);
    $statuses = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Format the response
    $formattedStatuses = [];
    foreach ($statuses as $status) {
        $friendsArray = [];
        if (!empty($status['friendsUpvoted'])) {
            $friendsArray = explode(', ', $status['friendsUpvoted']);
        }

        $formattedStatuses[] = [
            'statusId' => $status['statusId'],
            'statusText' => $status['statusText'],
            'upvotes' => (int)$status['upvotes'],
            'friendsUpvoted' => $friendsArray,
            'createdAt' => $status['createdAt']
        ];
    }
    
    echo json_encode([
        'status' => 'success',
          'message' => 'Status updates retrieved successfully',
        'myStatuses' => $formattedStatuses
    ]);
    
} catch (PDOException $e) {
    echo json_encode([
        'status' => 'error',
        'message' => 'Database error: ' . $e->getMessage()
    ]);
} catch (Exception $e) {
    echo json_encode([
        'status' => 'error',
        'message' => 'Error: ' . $e->getMessage()
    ]);
}
?>









