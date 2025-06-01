<?php

$db_host = "127.0.0.1";
$db_user = "s2543085";
$db_pass = "s2543085";
$db_name = "d2543085";

$conn = new mysqli($db_host, $db_user, $db_pass, $db_name);

if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Database connection fai>
}

$input = json_decode(file_get_contents('php://input'), true);

if (!isset($input['type']) || (!isset($input['username']) && !isset($input['ema>
    echo json_encode(["status" => "error", "message" => "Invalid request"]);
    exit;
}

$type = $conn->real_escape_string($input['type']);

if ($type === 'password') {
    
    if (!isset($input['username']) || !isset($input['new_password'])) {
        echo json_encode(["status" => "error", "message" => "Username and new p>
        exit;
    }
    
    $username = $conn->real_escape_string($input['username']);
    $new_password = password_hash($conn->real_escape_string($input['new_passwor>
    
    
    $stmt = $conn->prepare("UPDATE USER SET password = ? WHERE username = ?");
    $stmt->bind_param("ss", $new_password, $username);
    
} elseif ($type === 'username') {
    if (!isset($input['email'])) {
        echo json_encode(["status" => "error", "message" => "Email required"]);
        exit;
    }
    
    $email = $conn->real_escape_string($input['email']);
    
    if (!isset($input['new_username'])) {
        $stmt = $conn->prepare("SELECT username FROM USER WHERE email = ?");
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $result = $stmt->get_result();
        
        if ($result->num_rows > 0) {
            $user = $result->fetch_assoc();
            echo json_encode(["status" => "success", "username" => $user['usern>
        } else {
            echo json_encode(["status" => "error", "message" => "Email not foun>
        }
        $stmt->close();
        $conn->close();
        exit;
        } else {
        $new_username = $conn->real_escape_string($input['new_username']);
        $stmt = $conn->prepare("UPDATE USER SET username = ? WHERE email = ?");
        $stmt->bind_param("ss", $new_username, $email);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request type"]>
    exit;
}

if ($stmt->execute()) {
    if ($stmt->affected_rows > 0) {
        echo json_encode(["status" => "success", "message" => "Update successfu>
    } else {
        echo json_encode(["status" => "error", "message" => "No records updated>
    }
} else {
    echo json_encode(["status" => "error", "message" => "Update failed: " . $st>
}

$stmt->close();
$conn->close();
?>



        
        




