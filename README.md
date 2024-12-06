Personal Finance Assistant

CSC 335 - Object-Oriented Programming and Design

Team Members:
- Harshit Jain
- Advik Bargoti
- Ashish Choudhary
- Richard Posthuma

Project Overview
Personal Finance Assistant is a Java desktop application that helps users manage their personal finances. The application demonstrates the implementation of object-oriented design principles, GUI development using Swing, and secure user data management.


Key Classes and Functionality

Main Functionality Classes
•	FinanceApp: Main application window implementing tabbed interface for expenses, budgets, and reports.
•	ExpenseManager: Manages expense data and operations, including adding, editing, and filtering expenses.
•	BudgetManager: Handles budget allocations and tracks spending against budgets.
•	ReportManager: Generates financial reports and provides data visualization.

User Interface Classes
•	DashboardPanel: Shows financial overview and recent transactions.
•	ExpenseTrackerPanel: Provides interface for managing expenses with filtering capabilities.
•	BudgetManagerPanel: Displays budget progress with visual indicators for spending thresholds
•	ReportManagerPanel: Shows financial reports with pie charts and spending analysis

Data & Security Classes
•	User: Handles user authentication with secure password hashing
•	DataPersistenceManager: Manages file-based storage of user data and transactions
•	TransactionFileHandler: Handles CSV import/export functionality

Features
•	User authentication and account management
•	Expense tracking and categorization
•	Monthly budget management with visual progress tracking
•	Financial reporting with charts
•	Data persistence and file handling
•	Transaction import/export

How to Run
1.	Place the Project in you preferred IDE and run Main.java
2.	The application will automatically create necessary data directories 
3.	Use "Create Account" to register a new user 
4.	Login with your credentials
5.	Add expenses through the Expense tab 
6.	Set budgets in the Budget tab 
7.	View reports in the Reports tab
8.	Can switch between different months of data in Budget and Report Panels.
9.	Try importing/exporting transactions using CSV files

Testing
JUnit tests are provided for core functionality including:
- User authentication
- Expense operations
- Budget calculations
- Data persistence


Individual Contributions
Harshit Jain:
- User authentication system
- User interface design

Ashish Choudhary:
- Expense tracking functionality
- Data persistence implementation

Advik Bargoti:
- Budget management system
- Import/export Functionality

Richard Posthuma:
- Reporting system
- Data visualization


Documentation:
Each individual worked on their respective files for documentation and testing.

