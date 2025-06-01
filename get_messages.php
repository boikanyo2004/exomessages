<?php

ini_set('display_errors', 0);
error_reporting(E_ALL);

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

try {
    
    $servername = "localhost";
    $username = "s2543085";
    $password = "s2543085"; 
    $dbname = "d2543085";

   
    $conn = new mysqli($servername, $username, $password, $dbname);

    
    if ($conn->connect_error) {
        throw new Exception("Connection failed: " . $conn->connect_error);
    }

 
    $userId1 = isset($_POST['userId1']) ? intval($_POST['userId1']) : 0;
    $userId2 = isset($_POST['userId2']) ? intval($_POST['userId2']) : 0;

    
    if ($userId1 <= 0 || $userId2 <= 0) {
        echo json_encode([
            "status" => "error",
            "message" => "Missing or invalid required fields",
            "debug" => [
                "userId1" => $userId1,
                "userId2" => $userId2,
                "post_data" => $_POST
            ]
        ]);
        exit;
    }
    
    $friendCheck = "SELECT COUNT(*) as count FROM FRIENDSHIP 
                    WHERE (userId = ? AND friendId = ?) 
                    OR (userId = ? AND friendId = ?)";
    $stmt = $conn->prepare($friendCheck);
    if (!$stmt) {
        throw new Exception("Prepare failed: " . $conn->error);
    }
    
    $stmt->bind_param("iiii", $userId1, $userId2, $userId2, $userId1);
    $stmt->execute();
    $result = $stmt->get_result();
    $row = $result->fetch_assoc();

    if ($row['count'] == 0) {
        echo json_encode([
            "status" => "error",
            "message" => "You can only view messages with your friends"
        ]);
        exit;
    }
    
     $messagesQuery = "SELECT m.id, m.sender_id as senderId, m.receiver_id as receiverId, 
                             m.message, m.created_at as createdAt,
                             u.username as senderUsername
                      FROM messages m
                      JOIN USER u ON m.sender_id = u.userId
                      WHERE (m.sender_id = ? AND m.receiver_id = ?) 
                         OR (m.sender_id = ? AND m.receiver_id = ?)
                      ORDER BY m.created_at ASC";
    $stmt = $conn->prepare($messagesQuery);
    if (!$stmt) {
        throw new Exception("Prepare failed: " . $conn->error);
    }
    
    $stmt->bind_param("iiii", $userId1, $userId2, $userId2, $userId1);
    $stmt->execute();
    $result = $stmt->get_result();

    $messages = [];
    while ($row = $result->fetch_assoc()) {
        $messages[] = [
            "id" => intval($row['id']),
            "senderId" => intval($row['senderId']),
            "receiverId" => intval($row['receiverId']),
            "message" => $row['message'],
            "createdAt" => $row['createdAt'],
            "senderUsername" => $row['senderUsername']
        ];
    }
    
    echo json_encode([
        "status" => "success",
        "message" => count($messages) > 0 ? "Messages retrieved successfully" : "No messag>
        "messages" => $messages,
        "count" => count($messages)
    ]);

    $stmt->close();
    $conn->close();

} catch (Exception $e) {
    echo json_encode([
        "status" => "error",
        "message" => "Server error: " . $e->getMessage(),
        "debug" => [
            "file" => __FILE__,
            "line" => $e->getLine()
        ]
    ]);
}
?>



    
    


                
