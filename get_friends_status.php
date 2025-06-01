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

if (!isset($input['userId'])) {
    echo json_encode(["status" => "error", "message" => "User ID missing"]);
    exit;
}

$userId = $conn->real_escape_string($input['userId']);

$stmt = $conn->prepare("
    SELECT s.statusId, s.userId, u.username, u.fullName, s.statusText, s.createdAt 
    FROM STATUS_UPDATES s
    JOIN USER u ON s.userId = u.userId
    WHERE s.userId IN (
        SELECT friendId FROM FRIENDSHIP WHERE userId = ?
    )
    ORDER BY s.createdAt DESC
");

$stmt->bind_param("i", $userId);
$stmt->execute();
$result = $stmt->get_result();
$statusUpdates = array();
while ($row = $result->fetch_assoc()) {
    $statusUpdates[] = array(
        "statusId" => $row['statusId'],
        "userId" => $row['userId'],
        "username" => $row['username'],
        "fullName" => $row['fullName'],
        "statusText" => $row['statusText'],
        "createdAt" => $row['createdAt']
    );
}

if (count($statusUpdates) > 0) {
    echo json_encode([
        "status" => "success",
        "message" => "Status updates retrieved successfully",
        "statusUpdates" => $statusUpdates
    ]);
} else {
    echo json_encode([
        "status" => "success",
        "message" => "No status updates found",
        "statusUpdates" => []
    ]);
}

if (count($statusUpdates) > 0) {
    echo json_encode([
        "status" => "success",
        "message" => "Status updates retrieved successfully",
        "statusUpdates" => $statusUpdates
    ]);
} else {
    echo json_encode([
        "status" => "success",
        "message" => "No status updates found",
        "statusUpdates" => []
    ]);
}

$stmt->close();
$conn->close();
?>


