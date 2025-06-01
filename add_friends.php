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

if (!isset($input['userId']) || !isset($input['friendId'])) {
    echo json_encode(["status" => "error", "message" => "User ID or Friend ID m>
    exit;
}

$userId = $conn->real_escape_string($input['userId']);
$friendId = $conn->real_escape_string($input['friendId']);

$stmt = $conn->prepare("SELECT * FROM FRIENDSHIP WHERE userId = ? AND friendId >
$stmt->bind_param("ii", $userId, $friendId);
$stmt->execute();
$stmt->store_result();

if ($stmt->num_rows > 0) {
    echo json_encode(["status" => "error", "message" => "Already friends"]);
    $stmt->close();
    $conn->close();
    exit;
}
$stmt->close();

$stmt = $conn->prepare("SELECT * FROM USER WHERE userId = ?");
$stmt->bind_param("i", $friendId);
$stmt->execute();
$stmt->store_result();

if ($stmt->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "User not found"]);
    $stmt->close();
    $conn->close();
    exit;
}
$stmt->close();

$stmt = $conn->prepare("INSERT INTO FRIENDSHIP (userId, friendId, createdAt) VA>
$stmt->bind_param("ii", $userId, $friendId);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Friend added success>
} else {
    echo json_encode(["status" => "error", "message" => "Failed to add friend"]>
}

$stmt->close();
$conn->close();
?>

