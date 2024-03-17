# Beta Testing/Development Setup
- If you are a Beta-tester that does not have a smartphone device with Android 9 (Pie) or up installed, then you can follow these steps to run the WalletWatch app in an emulator on your computer.
- If you would like to contribute to this open-source project, then these steps will get you setup in the development environment.

## Step-By-Step Walkthrough
1. Download the appropriate installer file for your computer from this link:
- https://developer.android.com/studio
2. Locate the downloaded installer file in your _Downloads_ folder on your computer.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/1.png "Locate Downloaded Installer File")

3. Double-click the installer file to run the installation of Android Studio. Windows users should preferably right-click the file and then click _Run as administrator_, if able. You may also refer to this link for additional installation instructions, especially if your computer is a Linux machine:
- https://developer.android.com/studio/install

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/2.png "Run Installer File")

4. Click _Next >_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/3.png "Click Next")

5. Make sure both the _Android Studio_ checkbox and _Android Virtual Device_ checkbox are checked. Then, click _Next >_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/4.png "Check Android Virtual Device and Then Click Next")

6. If you would prefer a different installation location, then select or enter the desired location. Then, click _Next >_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/5.png "Click Next")

7. If you would prefer not to have an Android Studio shortcut on your desktop, select _Do not create shortcuts_. Then, click _Install_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/6.png "Click Install")

8. Wait for the installation of Android Studio to complete. This will take several minutes. Then, click _Next >_ when it has completed.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/7.png "Wait for Installation to Complete and Then Click Next")

9. Make sure the _Start Android Studio_ checkbox is checked. Then, click _Finish_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/8.png "Click Finish")

10. A popup will appear. Check the _Do not import settings_ checkbox. Then, click _OK_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/9.png "Click OK")

11. On the next popup, select whether or not you want to allow Google the ability to collect anonymous usage data. 

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/10.png "Click Don't Send")

12. The Setup Wizard for Android Studio will appear. Click _Next_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/11.png "Click Next")

13. Check the _Standard_ checkbox. Then, click _Next_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/12.png "Click Next")

14. Check the checkbox of your preferred theme. Then, click _Next_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/13.png "Click Next")

15. Click _Next_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/14.png "Click Next")

16. Click _Finish_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/15.png "Click Finish")

17. Wait for the installation of the Android SDK to finish. This will take several minutes. Then, click _Finish_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/16.png "Wait for Installation To Finish and Then Click Finish")

18. Download a ZIP file of the WalletWatch GitHub repo by clicking this link:
- https://github.com/thedavidang/CSC4151/archive/master.zip

19. Locate the downloaded ZIP file in your _Downloads_ folder on your computer. Right click the ZIP file. Then, click _Extract All..._.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/17.png "Locate Downloaded ZIP File")

20. Click _Extract_ to extract the repo files out of the ZIP file.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/18.png "Click Extract")

21. Android Studio should have popped up a small welcoming window. Click _Open an existing Android Studio project_. In the navigation popup, locate the _Development_ folder within the extracted repo folder. The _Development_ folder should have a green Android robot icon next to it.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/19.png "Open existing Android Studio project using Development Folder")

22. Android Studio should now launch and load the WalletWatch project. Wait for Android Studio to finish automatically installing the libraries and other modules needed to run WalletWatch. This will take several minutes. You may recieve an Android SDK warning popup, which you may ignore. You may recieve a firewall warning popup, in which case you should allow Android Studio the ability to use the Java SDK.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/20.png "Wait for Android Studio to Finish Loading Project and Ignore Warning Messages")

23. Once Android Studio has finished running the background processes of installing the modules, click the _No Devices_ drop-down menu in the top menubar. Then, click _Open AVD Manager_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/21.png "Open AVD Manager")

24. The AVD Manager wizard will popup, which lets you create an emulator of an Android smartphone device, which acts as a virtual machine within Android Studio. Click _+ Create Virtual Device..._.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/22.png "Click Create Virtual Device")

25. Select a Phone that supports Android 9 (Pie) or up, such as the _Pixel 3_. Then, click _Next_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/23.png "Click Pixel 3 and Then Click Next")

26. Click _Download_ next to the desired Android 9+ version, such as Android 10 _Q_, to download the Android SDK on to the virtual device.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/24.png "Download Android 10 (Q)")

27. A popup will appear asking you to agree to the terms of two different license agreements. Click _Accept_ to both. Then, click _Next_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/25.png "Click Accept to Both License Agreements and Then Click Next")

28. If you would like to set a different virtual memory size, then drag the slider as desired. Otherwise, use the recommended size. Then, click _Next_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/26.png "Click Next")

29. Wait for the installation of the virtual device and Android SDK to finish. This will take several minutes. Then, click _Finish_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/27.png "Click Finish")

30. Click _Finish_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/28.png "Click Finish")

31. Click _Next_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/29.png "Click Next")

32. Make sure the starting orientation is set to _Portait_. Then, click _Finish_.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/30.png "Click Finish")

33. The AVD Manager will close. Now to start the emulator and run the WalletWatch app within it, click the green arrow play button in the top menubar. Alternatively, you may use the "Shift+F10" key command.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/31.png "Click Run to Launch the Emulator")

34. Wait for the emulator to startup. This will take a couple minutes. The emulator will automatically bypass the lock screen and then install the WalletWatch app on to the emulator. This will take a couple more minutes. Finally, the emulator will launch the WalletWatch app.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/32.png "Wait for WalletWatch to Launch within the Emulator")

35. When you are done testing the app, you may close the emulator by long-clicking the power button in the emulator sidebar and then clicking _Power Off_ within the emulator. Alternatively, you may simply click the tiny _x_ button in the emulator sidebar. You may then close Android Studio.

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/33.png "Close the Emulator and Close Android Studio")