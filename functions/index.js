const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendChatNotification = functions.firestore
  .document("chat_messages/{messageId}")
  .onCreate(async (snap, context) => {
    const newValue = snap.data();
    
    // In our prototype, 'chat_id' is effectively the recipient's user ID.
    // Ensure we have a chat_id and a text message.
    const recipientId = newValue.chat_id;
    const messageText = newValue.text;
    
    if (!recipientId || !messageText) {
      console.log("Missing recipientId or message text. Aborting notification.");
      return null;
    }

    // Optional: we don't have explicit sender_id in the message yet (fromUser = true is used instead).
    // In a real app, we'd lookup the sender's name. We'll use a generic title for now.
    const title = "New Message in Mitron";
    const body = messageText.length > 50 ? messageText.substring(0, 50) + "..." : messageText;

    try {
      // Get the recipient's FCM token from the users collection
      const userDoc = await admin.firestore().collection("users").doc(recipientId).get();
      if (!userDoc.exists) {
        console.log(`No user found for ID: ${recipientId}`);
        return null;
      }
      
      const userData = userDoc.data();
      const fcmToken = userData.fcmToken;
      
      if (!fcmToken) {
        console.log(`User ${recipientId} does not have an FCM token registered.`);
        return null;
      }
      
      const payload = {
        token: fcmToken,
        notification: {
          title: title,
          body: body,
        },
        data: {
          click_action: "FLUTTER_NOTIFICATION_CLICK", // Or whatever action starts your Android intent
          chatId: recipientId // Send back the chat ID so we can deep-link
        }
      };
      
      // Send the message
      const response = await admin.messaging().send(payload);
      console.log(`Successfully sent message to ${recipientId}:`, response);
      
    } catch (error) {
      console.error("Error sending push notification:", error);
    }
    
    return null;
  });
