# H&R 2020 - VR Robot Explorer
 **[Our Devpost](https://devpost.com/software/hnr2020-vr-robot)**

This is basically a robot which is controlled by VR. The robot has two cameras and some sensors which are used to give users data about the environment the robot is in. Input from the camera and sensors is displayed on the VR headset. The clients can rotate their headset, which will cause the robot's camera to rotate in the same direction, and by touching the side button of the headset, the robot will start or stop moving.

The project consists of 4 main modules: The client, server, Raspberry Pi and Arduino. All 4 modules communicate with each other either directly or through another module to ensure that the robot functions as expected.

## Module list
 - [Client Module](https://github.com/team-unununium/HnR-2020-VR-Client)
 - [Server Module](https://github.com/team-unununium/HnR-2020-VR-Server)
 - [Raspberry Pi Module](https://github.com/team-unununium/HnR-2020-VR-Pi)
 - [Arduino Module](https://github.com/team-unununium/HnR-2020-VR-Arduino)

# Current module - Client
The client module displays the video feed and updates the status of the robot to the user. It communicates to the server with a [Socket.IO](https://github.com/socketio/socket.io-client-java) connection and receives the video feed from the robot via Twitch. Some of the code in the project is from [Google's Cardboard VR SDK Sample Implementation](https://github.com/googlevr/gvr-android-sdk) which we used as a reference. The identity of the client is confirmed by the server through two common secrets, the `SERVER_OPERATOR_SECRET` and `SERVER_CLIENT_SECRET` respectively.

## How to install
APKs of the latest release would be available on the repository's [release page](https://github.com/team-unununium/HnR-2020-VR-Client/releases). If you wish to make changes to the app, you may have to compile the app yourself either through the command line or through an IDE like [Android Studio](https://developer.android.com/studio).
 
## Compiling Notes

 - The `SERVER_CLIENT_SECRET` and `SERVER_OPERATOR_SECRET` environmental variables are provided in a file called `secure.properties` in the root directory. If it is empty, you may need to create it.
 - The `applicationId` in `app/build.gradle` may need to be changed to prevent any conflict with the current app.
 - The list of the environmental variables that need to be set in `keystore.properties` are show below:
   - `keystoreDir`: The file in which the keystore is contained.
   - `keystoreAlias`: The alias for the keystore used to sign the app.
   - `keystorePass`: The password for the keystore used to sign the app.
 - The list of the environmental variables that need to be set in `secure.properties` are shown below:
   - `SERVER_OPERATOR_SECRET`: The secret used by the server to identify the client as an operator.
   - `SERVER_CLIENT_SECRET`: The secret used by the server to identify the client as an observer.
   - `SERVER_URL`: The URL that the client connects to.
   - `JITSI_ROOM_URL`: The URL of the Jitsi Meet room that is used to receive the video footage sent by the robot.
   - `JITSI_ROBOT_USER`: The display name of the Pi that is transmitting the video in the Jitsi Meet room.
 - String environmental variables may need to be encased in double quotes.

# If you wish to help

## Contributing
Any contribution is welcome, feel free to add any issues or pull requests to the repository.

## Licenses
This project is licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html).