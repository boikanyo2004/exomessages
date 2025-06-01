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
    $conn = new mysqli($servername, $username, $password, $dbname);
    
    if ($conn->connect_error) {
        throw new Exception("Database connection failed: " . $conn->connect_error);
    }
    
    // Get POST data
    $input = json_decode(file_get_contents('php://input'), true);
    $userId = isset($input['userId']) ? (int)$input['userId'] : 0;
    
    if ($userId <= 0) {
        echo json_encode([
            'status' => 'error',
            'message' => 'Invalid user ID'
        ]);
        exit;
    }
    
    $query = "
        SELECT 
            s.statusId,
            s.statusText,
            s.createdAt,
            COUNT(u.upvoteId) as upvotes,
            GROUP_CONCAT(us.username SEPARATOR ', ') as friendsUpvoted
        FROM STATUS_UPDATES s
        LEFT JOIN STATUS_UPVOTES u ON s.statusId = u.statusId
        LEFT JOIN USER us ON u.userId = us.userId AND us.userId != s.userId
        WHERE s.userId = ?
        GROUP BY s.statusId, s.statusText, s.createdAt
        ORDER BY s.createdAt DESC
    ";
    
    $stmt = $conn->prepare($query);
    $stmt->bind_param("i", $userId);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $statuses = [];
    while ($row = $result->fetch_assoc()) {
        $statuses[] = $row;
    }
    
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
    
    $stmt->close();
    $conn->close();
    
} catch (Exception $e) {
    echo json_encode([
        'status' => 'error',
        'message' => 'Error: ' . $e->getMessage()
    ]);
}
?>



    
    

    
    
    


