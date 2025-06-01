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
    SELECT u.userId, u.username, u.fullName, u.email, f.createdAt 
    FROM FRIENDSHIP f 
    JOIN USER u ON f.friendId = u.userId 
    WHERE f.userId = ? 
    ORDER BY f.createdAt DESC
");

$stmt->bind_param("i", $userId);
$stmt->execute();
$result = $stmt->get_result();

$friends = array();
while ($row = $result->fetch_assoc()) {
    $friends[] = array(
        "userId" => $row['userId'],
        "username" => $row['username'],
        "fullName" => $row['fullName'],
        "email" => $row['email'],
        "createdAt" => $row['createdAt']
    );
}

if (count($friends) > 0) {
    echo json_encode([
        "status" => "success",
        "message" => "Friends retrieved successfully",
        "friends" => $friends
    ]);
} else {
    echo json_encode([
        "status" => "success",
        "message" => "No friends found",
        "friends" => []
    ]);
}

$stmt->close();
$conn->close();
?>

