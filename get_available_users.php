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

if (!isset($input['userId'])) {
    echo json_encode(["status" => "error", "message" => "User ID missing"]);
    exit;
}

$userId = $conn->real_escape_string($input['userId']);

$stmt = $conn->prepare("
    SELECT u.userId, u.username, u.fullName, u.email 
    FROM USER u 
    WHERE u.userId != ? 
    AND u.userId NOT IN (
        SELECT f.friendId 
        FROM FRIENDSHIP f 
        WHERE f.userId = ?
    )
    ORDER BY u.username ASC
");

$stmt->bind_param("ii", $userId, $userId);
$stmt->execute();
$result = $stmt->get_result();

$users = array();
while ($row = $result->fetch_assoc()) {
    $users[] = array(
        "userId" => $row['userId'],
        "username" => $row['username'],
        "fullName" => $row['fullName'],
        "email" => $row['email']
    );
}

if (count($users) > 0) {
    echo json_encode([
        "status" => "success",
        "message" => "Available users retrieved successfully",
        "users" => $users
    ]);
} else {
    echo json_encode([
        "status" => "success",
        "message" => "No users available to add as friends",
        "users" => []
    ]);
}

$stmt->close();
$conn->close();
?>
