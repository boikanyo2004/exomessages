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

if (!isset($input['username']) || !isset($input['fullname']) || !isset($input['>
    echo json_encode(["status" => "error", "message" => "All fields are require>
    exit;
}

$username = $conn->real_escape_string($input['username']);
$fullname = $conn->real_escape_string($input['fullname
$email = $conn->real_escape_string($input['email']);
$password = password_hash($input['password'], PASSWORD_DEFAULT);

$stmt = $conn->prepare("SELECT userId FROM USER WHERE username = ?");
$stmt->bind_param("s", $username);
$stmt->execute();
$stmt->store_result();

if ($stmt->num_rows > 0) {
    echo json_encode(["status" => "error", "message" => "Username already exist>
    $stmt->close();
    $conn->close();
    exit;
}
$stmt->close();

$stmt = $conn->prepare("SELECT userId FROM USER WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$stmt->store_result();

if ($stmt->num_rows > 0) {
    echo json_encode(["status" => "error", "message" => "Email already exists"]>
    $stmt->close();
    $conn->close();
    exit;
}
$stmt->close();

$stmt = $conn->prepare("INSERT INTO USER (username, fullName, email, password, >
$stmt->bind_param("ssss", $username, $fullname, $email, $password);

if ($stmt->execute()) {
    $newUserId = $conn->insert_id;
    echo json_encode([
        "status" => "success", 
        "message" => "Registration successful! Welcome to EXO Message!",
        "userId" => $newUserId,
        "username" => $username,
        "fullName" => $fullname,
        "email" => $email
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Registration failed. P>
}

$stmt->close();
$conn->close();
?>



