<?php
header('Content-Type: application/json');

$conn = new mysqli("127.0.0.1", "s2543085", "s2543085", "d2543085");

if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Database connection failed"]));
}

$input = json_decode(file_get_contents('php://input'), true);

if (!isset($input['statusId']) || !isset($input['userId'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

$statusId = $conn->real_escape_string($input['statusId']);
$userId = $conn->real_escape_string($input['userId']);

$check = $conn->prepare("SELECT * FROM STATUS_UPVOTES WHERE statusId = ? AND userId = ?");
$check->bind_param("ii", $statusId, $userId);
$check->execute();

if ($check->get_result()->num_rows > 0) {
   
    echo json_encode([
        "status" => "success", 
        "message" => "Already upvoted",
        "newUpvoteCount" => getUpvoteCount($conn, $statusId)
    ]);
    exit;
}

$stmt = $conn->prepare("INSERT INTO STATUS_UPVOTES (statusId, userId) VALUES (?, ?)");
$stmt->bind_param("ii", $statusId, $userId);

if ($stmt->execute()) {
   
    $update = $conn->prepare("UPDATE STATUS_UPDATES SET upvotes = upvotes + 1 WHERE status>
    $update->bind_param("i", $statusId);
    $update->execute();
    
    echo json_encode([
        "status" => "success", 
        "message" => "Upvoted successfully",
        "newUpvoteCount" => getUpvoteCount($conn, $statusId)
    ]);
} else {
    echo json_encode([
        "status" => "error", 
        "message" => "Failed to upvote",
        "newUpvoteCount" => getUpvoteCount($conn, $statusId)
    ]);
}

$stmt->close();
$conn->close();

function getUpvoteCount($conn, $statusId) {
    $query = $conn->prepare("SELECT upvotes FROM STATUS_UPDATES WHERE statusId = ?");
    $query->bind_param("i", $statusId);
    $query->execute();
    $result = $query->get_result();
    $row = $result->fetch_assoc();
    return $row ? $row['upvotes'] : 0;
}
?>


