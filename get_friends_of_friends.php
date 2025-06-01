<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

$servername = "localhost";
$username = "s2543085";
$password = "s2543085";
$dbname = "d2543085";

try {
    
    $conn = new mysqli($servername, $username, $password, $dbname);
    
    
    if ($conn->connect_error) {
        throw new Exception("Connection failed: " . $conn->connect_error);
    }
    
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!isset($input['userId'])) {
        throw new Exception("User ID is required");
    }
    
    $userId = intval($input['userId']);
    
    $myFriendsSql = "SELECT u.userId, u.username, u.fullName 
                     FROM FRIENDSHIP f 
                     JOIN USER u ON f.friendId = u.userId 
                     WHERE f.userId = ?";
    
    $stmt = $conn->prepare($myFriendsSql);
    $stmt->bind_param("i", $userId);
    $stmt->execute();
    $myFriendsResult = $stmt->get_result();
    
    $friendsOfFriendsGrouped = array();
    
    while ($myFriend = $myFriendsResult->fetch_assoc()) {
        $friendId = $myFriend['userId'];

        

        $friendsFriendsSql = "SELECT u.userId, u.username, u.fullName, u.email 
                             FROM FRIENDSHIP f2 
                             JOIN USER u ON f2.friendId = u.userId 
                             WHERE f2.userId = ? 
                             AND u.userId != ? 
                             AND u.userId NOT IN (
                                 SELECT friendId FROM FRIENDSHIP WHERE userId =>
                             )";

        $stmt2 = $conn->prepare($friendsFriendsSql);
        $stmt2->bind_param("iii", $friendId, $userId, $userId);
        $stmt2->execute();
        $friendsFriendsResult = $stmt2->get_result();
        
        $friendsFriends = array();
        while ($friendOfFriend = $friendsFriendsResult->fetch_assoc()) {
            $friendsFriends[] = array(
                'userId' => intval($friendOfFriend['userId']),
                'username' => $friendOfFriend['username'],
                'fullName' => $friendOfFriend['fullName'],
                'email' => $friendOfFriend['email']
            );
        }
        
        if (!empty($friendsFriends)) {
            $friendsOfFriendsGrouped[] = array(
                'friendInfo' => array(
                    'userId' => intval($myFriend['userId']),
                    'username' => $myFriend['username'],
                    'fullName' => $myFriend['fullName']
                ),
                'theirFriends' => $friendsFriends
            );
        }

        $stmt2->close();
    }
    
    $stmt->close();
    $conn->close();
    
    echo json_encode(array(
        'status' => 'success',
        'message' => 'Friends of friends retrieved successfully',
        'friendsOfFriendsGrouped' => $friendsOfFriendsGrouped
    ));
    
} catch (Exception $e) {
   
    echo json_encode(array(
        'status' => 'error',
        'message' => $e->getMessage()
    ));
}
?>




    

       
