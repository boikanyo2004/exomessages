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

   
    $senderId = isset($_POST['senderId']) ? intval($_POST['senderId']) : 0;
    $receiverId = isset($_POST['receiverId']) ? intval($_POST['receiverId']) : 0;
    $message = isset($_POST['message']) ? trim($_POST['message']) : '';
    
    if ($senderId <= 0 || $receiverId <= 0 || empty($message)) {
        echo json_encode([
            "status" => "error",
            "message" => "Missing or invalid required fields",
            "debug" => [
                "senderId" => $senderId,
                "receiverId" => $receiverId,
                "message" => $message,
                "post_data" => $_POST
            ]
        ]);
        exit;
    }

    if ($senderId == $receiverId) {
        echo json_encode([
            "status" => "error",
            "message" => "Cannot send message to yourself"
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
    
    $stmt->bind_param("iiii", $senderId, $receiverId, $receiverId, $senderId);
    $stmt->execute();
    $result = $stmt->get_result();
    $row = $result->fetch_assoc();

    if ($row['count'] == 0) {
        echo json_encode([
            "status" => "error",
            "message" => "You can only send messages to your friends"
        ]);
        exit;
    }
    
      $insertQuery = "INSERT INTO messages (sender_id, receiver_id, message, created_at) 
                    VALUES (?, ?, ?, NOW())";
    $stmt = $conn->prepare($insertQuery);
    if (!$stmt) {
        throw new Exception("Prepare failed: " . $conn->error);
    }
    
    $stmt->bind_param("iis", $senderId, $receiverId, $message);

    if ($stmt->execute()) {
        echo json_encode([
            "status" => "success",
            "message" => "Message sent successfully",
            "messageId" => $conn->insert_id
        ]);
    } else {
        throw new Exception("Failed to insert message: " . $stmt->error);
    }

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


    
    
    




