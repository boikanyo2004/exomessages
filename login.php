<?php

header('Content-Type: application/json');


$db_host = "127.0.0.1";
$db_user = "s2543085";
$db_pass = "s2543085";
$db_name = "d2543085";

$conn = new mysqli($db_host, $db_user, $db_pass, $db_name);

if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Database connection fai>
}

$input = json_decode(file_get_contents('php://input'), true);



if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Database connection fai>
}



$input = json_decode(file_get_contents('php://input'), true);

if (!isset($input['username']) || !isset($input['password'])) {
    echo json_encode(["status" => "error", "message" => "Username and password >
    exit;
}

$username = $conn->real_escape_string($input['username']);
$inputPassword = $input['password'];

$stmt = $conn->prepare("SELECT userId, username, fullName, email, password FROM>
$stmt->bind_param("s", $username);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $user = $result->fetch_assoc();
    
    
    if (password_verify($inputPassword, $user['password'])) {
        
        echo json_encode([
            "status" => "success", 
            "message" => "Login successful! Welcome back!",
            "userId" => $user['userId'],
            "username" => $user['username'],
            "fullName" => $user['fullName'],
            "email" => $user['email']
        ]);
    } else {
        
        if ($inputPassword === $user['password']) {
            
            $hashedPassword = password_hash($inputPassword, PASSWORD_DEFAULT);
            $updateStmt = $conn->prepare("UPDATE USER SET password = ? WHERE us>
            $updateStmt->bind_param("si", $hashedPassword, $user['userId']);
            $updateStmt->execute();
            $updateStmt->close();

            
            echo json_encode([
                "status" => "success", 
                "message" => "Login successful! Password updated for security.",
                "userId" => $user['userId'],
                "username" => $user['username'],
                "fullName" => $user['fullName'],
                "email" => $user['email']
            ]);
        } else {
            echo json_encode(["status" => "error", "message" => "Invalid userna>
        }
    }
} else {
    echo json_encode(["status" => "error", "message" => "Invalid username or pa>
}

$stmt->close();
$conn->close();
?>


            

    
   
   







