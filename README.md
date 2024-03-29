# WalletWatch© by SpendSages©

### Self-discover your spending habits with a simple, secure, and offline Android app.

A Software Engineering Project by the SpendSages©:
- Matthew Paik
- Chandler Stevens
- David Ang
- Jason Djajasasmita

## Table of Contents
- [Introduction](https://github.com/thedavidang/CSC4151#introduction)
- [Latest Stable Version](https://github.com/thedavidang/CSC4151#latest-stable-version)
- [Minimum System Requirements](https://github.com/thedavidang/CSC4151#minimum-system-requirements)
- [Installation Instructions](https://github.com/thedavidang/CSC4151#installation-instructions)
- [Application Usage](https://github.com/thedavidang/CSC4151#application-usage)

## Introduction
<a href="http://www.youtube.com/watch?feature=player_embedded&v=fnWhzOBocUw
" target="_blank"><img src="http://img.youtube.com/vi/fnWhzOBocUw/0.jpg" 
alt="WalletWatch by SpendSages Feature Video" width="480" height="360" border="10" /></a>

https://youtu.be/fnWhzOBocUw

WalletWatch© is an app designed for budget-minded individuals that are unwilling to compromise privacy. This app lets **you** track your expense history, so that **you** can better watch your wallet. The app never even tries to connect to the Internet, so none of your spending history will ever be sold off across cloud databases. The app never asks for sensitive information and only helps you track the money you spend, not the money you make.

Not only is WalletWatch© securely offline, but it is also extremely quick and easy to use! A simplicity-driven design and intuitive user interface allow you the capability to rapidly add purchases and visualize charts of your aggregated expenses. This app is excellent for those who are juggling multiple bank accounts, want to discover how much money they spend in a particular category, or who simply want to keep their financial information more private.

## Latest Stable Version
- 1.1.2

## Recommended System Requirements
- Android 14 (Upside Down Cake) and up

## Minimum System Requirements
- Android 9 (Pie) and up

## Installation Instructions
### Available on the Google Play Store:

[![Image](https://github.com/thedavidang/CSC4151/blob/master/images/play.jpg)](https://play.google.com/store/apps/details?id=com.spendsages.walletwatch "Google Play Store Logo")

- https://play.google.com/store/apps/details?id=com.spendsages.walletwatch

### Alternative 1 - Download APK directly from this repo:
If you prefer not to install WalletWatch from the Google Play Store, then you may follow this step-by-step walkthrough to install the WalletWatch app directly on to your Android Device:
- https://github.com/thedavidang/CSC4151/blob/master/APK_SETUP.md

Basically, you will download and install the WalletWatch APK installer file from this link:
- https://github.com/thedavidang/CSC4151/raw/master/WalletWatch-v1.1.2-Installer.apk

### Alternative 2 - Download repo and run in Android Studio:
If you do not have an Android smartphone device or have a version of Android earlier than Android 9 (Pie) installed, then follow this step-by-step walkthrough to run the WalletWatch app in an emulator on your computer:
- https://github.com/thedavidang/CSC4151/blob/master/DEV_SETUP.md

## Application Usage
### Main Window
The main window of the app is organized into three tabs:
#### 1. **Add - For submitting a new purchase.** 

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/tab1.png "Tab 1: Add")

- Enter the dollar amount of the purchase and then tap one of the 3 category buttons to submit the entry.
- The Description field is optional and may be used as a reminder of what a purchase was for.
- The Date field provides the capability of entering a past date of the purchase, but not a future or recurring date.
- You may also visually select a past date by tapping the calendar button, which will open a calendar selector window.
- Whenever you launch the app or navigate to _Tab 1: Add_, the numpad will automatically open for you, which results in rapid submission of new purchases.
2. **Analytics - For viewing charts and totals of your aggregated expenses.** 

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/tab2a.png "Tab 2: Analytics Line Chart")
![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/tab2b.png "Tab 2: Analytics Pie Chart")

- The left drop-down menu lets you switch between a line chart view or a pie chart view. Selecting _Pie_ will disable the drop-down menu on the right.
- The middle drop-down menu changes the time interval of the aggregated data. This will update the currently selected chart and the totals displayed below the chart. _Last 7 Days_ will give you an idea of your spending habits over the past week. _Last 12 Months_ will help you discover your spending trends over the past year. _All Time_ will aggregate all your expenses, which will show you the areas you have been spending most ever since you started using WalletWatch. The line chart will only display the last 10 years as to focus in on the last decade of purchases.
- The drop-down menu on the right lets you select which category you would like to filter to on the line chart. This will not change the totals displayed below the line chart. The _All_ option aggregates the expenses from all three categories together for a consolidated view.
3. **History - For viewing, editing, and deleting existing expenses.** 

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/tab3.png "Tab 3: History")
![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/edit.png "Edit Expense Window")

- The left drop-down menu allows you to **sort** the list of expenses. You can sort by either date or purchase price and in either descending or ascending order.
- The drop-down menu on the right allows you to **filter** the list of expenses by category. _All_ shows expenses from all three categories for a complete view of your expense history.
- The checkboxes to the left of each expense let you select which expenses you would like to permanately delete from your history.
- Once you have checked off the expenses that you want to delete, tap the _Delete Selected_ button to remove them from your history.
- Tapping the pencil icon button to the right of each expense provides you the capability of modifying the expense. This will open a window almost identical to _Tab 1: Add_, which allows you to easily edit one or more fields of the expense.

### Settings Window
The settings window can be accessed and exited by tapping the wrench icon in the top-right corner of the screen. The settings window of the app is also organized into three tabs:
1. **Categories - For changing or restoring category labels.** 

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/categories.png "Categories")

- Simply type a new category label into one of the three textboxes to stop tracking one category of expenses and start tracking a new one. The old category and all of its data will be archived on to your Android device. If you change a category to a category that you used to be tracking, all the archived data of that category will be restored.
2. **Terms - For viewing the Privacy Policy, Terms & Conditions, and MIT License associated with this free, open-source app.** 

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/terms.png "Terms")

3. **About - For viewing version information about this app.** 

![alt text](https://github.com/thedavidang/CSC4151/blob/master/images/about.png "About")
