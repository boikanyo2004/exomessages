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
    
    
    $input = json_decode(file_get_contents('php://input'), true);
    $userId = isset($input['userId']) ? (int)$input['userId'] : 0;
    $statusId = isset($input['statusId']) ? (int)$input['statusId'] : 0;
    
    if ($userId <= 0) {
        echo json_encode([
            'status' => 'error',
            'message' => 'Invalid user ID'
        ]);
        exit;
    }
    
    if ($statusId <= 0) {
        echo json_encode([
            'status' => 'error',
            'message' => 'Invalid status ID'
        ]);
        exit;
    }
    
     $conn->begin_transaction();
    
    try {
        
        $verifyQuery = "SELECT userId FROM STATUS_UPDATES WHERE statusId = ?";
        $verifyStmt = $conn->prepare($verifyQuery);
        $verifyStmt->bind_param("i", $statusId);
        $verifyStmt->execute();
        $verifyResult = $verifyStmt->get_result();

        if ($verifyResult->num_rows === 0) {
            throw new Exception("Status not found");
        }

        $statusOwner = $verifyResult->fetch_assoc();
        if ($statusOwner['userId'] != $userId) {
            throw new Exception("You can only delete your own status updates");
        }

       
        $deleteUpvotesQuery = "DELETE FROM STATUS_UPVOTES WHERE statusId = ?";
        $deleteUpvotesStmt = $conn->prepare($deleteUpvotesQuery);
        $deleteUpvotesStmt->bind_param("i", $statusId);
        $deleteUpvotesStmt->execute();

       
        $deleteStatusQuery = "DELETE FROM STATUS_UPDATES WHERE statusId = ? AND userId = ?>
        $deleteStatusStmt = $conn->prepare($deleteStatusQuery);
        $deleteStatusStmt->bind_param("ii", $statusId, $userId);
        $deleteStatusStmt->execute();

        if ($deleteStatusStmt->affected_rows === 0) {
            throw new Exception("Failed to delete status update");
        }
        
        
        $conn->commit();

        echo json_encode([
            'status' => 'success',
            'message' => 'Status update deleted successfully'
        ]);

        $verifyStmt->close();
        $deleteUpvotesStmt->close();
        $deleteStatusStmt->close();

    } catch (Exception $e) {
    
        $conn->rollback();
        throw $e;
    }
    
    $conn->close();
    
} catch (Exception $e) {
    echo json_encode([
        'status' => 'error',
        'message' => 'Error: ' . $e->getMessage()
    ]);
}
?>


        
        


