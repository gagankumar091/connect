import re

with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

# Add permissions
if "android.permission.FOREGROUND_SERVICE" not in content:
    content = content.replace("<uses-permission android:name=\"android.permission.POST_NOTIFICATIONS\" />", 
                              "<uses-permission android:name=\"android.permission.POST_NOTIFICATIONS\" />\n    <uses-permission android:name=\"android.permission.FOREGROUND_SERVICE\" />\n    <uses-permission android:name=\"android.permission.FOREGROUND_SERVICE_DATA_SYNC\" />")

# Replace service
service_regex = r"<service\s+android:name=\"\.services\.MitronFirebaseMessagingService\"[\s\S]*?</service>"
new_service = """<service
            android:name=".services.MitronSyncService"
            android:foregroundServiceType="dataSync"
            android:exported="false" />"""
content = re.sub(service_regex, new_service, content)

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)

print("Manifest patched")
