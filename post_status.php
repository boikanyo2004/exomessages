<?php
header('Content-Type: application/json');

$db_host = "127.0.0.1";
$db_user = "s2543085";
$db_pass = "s2543085";
$db_name = "d2543085";

$conn = new mysqli($db_host, $db_user, $db_pass, $db_name);

if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Database connection failed"]));
}

$input = json_decode(file_get_contents('php://input'), true);

if (!isset($input['userId']) || !isset($input['statusText'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

$userId = $conn->real_escape_string($input['userId']);
$statusText = $conn->real_escape_string($input['statusText']);

$stmt = $conn->prepare("INSERT INTO STATUS_UPDATES (userId, statusText) VALUES (?, ?)");
$stmt->bind_param("is", $userId, $statusText);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Status posted successfully"]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to post status"]);
}

$stmt->close();
$conn->close();
?>



