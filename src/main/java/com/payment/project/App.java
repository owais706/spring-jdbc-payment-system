package com.payment.project;

import java.util.List;
import java.util.Scanner;

import javax.sql.DataSource;

import com.payment.project.entities.Transaction;
import com.payment.project.entities.User;
import com.payment.project.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

public class App
{
	private static UserRepository userRepo;
	private User currentUser;
	
	public App() {
		userRepo=new UserRepository();
	}
	
	public void showMenu() {
		Scanner sc=new Scanner(System.in);
		
		boolean exit=true;
		
		while(exit) {
			if(currentUser==null) {
				System.out.println("1. Register new user");
				System.out.println("2. Login");
				System.out.println("3. Exit");
				System.out.println("Select an option:");
				int choice=sc.nextInt();sc.nextLine();
				
				switch(choice) {
					case 1:
						createAccount();
						break;
					case 2:
						login();
						break;
					case 3:
						exit=false;
						System.out.println("Exited Sucessfully");
						break;
					default:
						System.out.println("Invalid option....Try again !\n");
				}
			} else {
				System.out.println("1. Show balance");
				System.out.println("2. Deposit Money");
				System.out.println("3. Withdraw Money");
				System.out.println("4. Transfer Money to other User ");
				System.out.println("5. Print transaction history");
				System.out.println("6. Change your PIN number");
				System.out.println("7. Logout");
				System.out.println("8. Exit");
				System.out.println("Select an option:");
				
				int choice=sc.nextInt();sc.nextLine();
				
				switch(choice) {
				case 1:
					showBalance();
					break;
				case 2:
					depositMoney();
					break;
				case 3:
					withdrawMoney();
					break;
				case 4:
					fundTransfer();
					break;
				case 5:
					printTransactionHistory();
					break;

				case 6:
					changUserPin();
					break;

				case 7:
					logout();
					break;
				case 8:
					exit=false;
					System.out.println("Exited Sucessfully");
					break;
				default:
					System.out.println("Invalid option....Try again !\n");
			}
			}
		}
	}

	private void changUserPin() {
		Scanner sc=new Scanner(System.in);
		System.out.println("Enter your Old PIN : ");
		String oldPin=sc.nextLine();
		System.out.println("Enter your New PIN: ");
		String newPin=sc.nextLine();
		System.out.println("Re-Enter your New PIN: ");
		String newPinCheck=sc.nextLine();

		if(!oldPin.equals(currentUser.getUserPin())){
			System.out.println("!! You have entered a wrong PIN . Please fill correct one. \n" );
			return;
		}
		else if(!newPin.equals(newPinCheck) ){
			System.out.println("!! PIN is not matching . Please try again...\n");
			return;
		}
		else if(oldPin.equals(newPinCheck)) {
			System.out.println("!! Your new PIN can't be same as your previous PIN. Please Try a different one \n");
			return;

		}
		else{
			currentUser.setUserPin(newPinCheck);
			userRepo.changeUserPin(currentUser,newPin);
			System.out.println("PIN changed successfully.... \n");
			return;
		}



	}

	private void createAccount() {
		Scanner sc=new Scanner(System.in);
		System.out.println("Enter user name: ");
		String userName=sc.nextLine();
		System.out.println("Enter password: ");
		String userPassword=sc.nextLine();
		System.out.println("Enter user email: ");
		String userEmail=sc.nextLine();
		System.out.println("Enter user mobile no: ");
		String userNumber=sc.nextLine();
		userRepo.addUser(userName, userPassword, userEmail,userNumber);
		
	}
	
	private void login() {
		Scanner sc=new Scanner(System.in);
		System.out.println("Enter your Phone number: ");
		String userNumber=sc.nextLine();
		System.out.println("Enter your password: ");
		String userPassword=sc.nextLine();
		
		currentUser=userRepo.authenticateUser(userNumber, userPassword);
		if(currentUser!=null) {
			System.out.println("Login Successfull\n");
		} else {
			System.out.println("Invalid user name or password\n");
		}
	}
	
	private void logout() {
		currentUser=null;
		System.out.println("Logged out...!!\n");
	}
	
	private void showBalance() {
		if(currentUser==null) {
			System.out.println("Please login first.");
			return;
		}
		System.out.println("Current balance : " + currentUser.getBalance()+"\n");
	}
	
	public void depositMoney() {
		if(currentUser==null) {
			System.out.println("Please login first.");
			return;
		}
		Scanner sc=new Scanner(System.in);
		System.out.println("Enter amount to deposit : ");
		double amount=sc.nextDouble();
		currentUser.setBalance(currentUser.getBalance()+amount);
		
		userRepo.updateUserBalance(currentUser.getUserId(), currentUser.getBalance());
		
		Transaction transaction=new Transaction();
		transaction.setUserId(currentUser.getUserId());
		transaction.setAmount(amount);
		transaction.setTransactionType("Deposit");
		
		userRepo.addTransaction(transaction);
		System.out.println("\nMoney Deposited Successfully..!!");
		System.out.println("Updated Balance : "+currentUser.getBalance()+"\n");
	}
	
	public void withdrawMoney() {
		if(currentUser==null) {
			System.out.println("Please login first.");
			return;
		}
		Scanner sc=new Scanner(System.in);
		System.out.println("Enter amount to withdraw : ");
		double amount=sc.nextDouble();sc.nextLine();
		
		System.out.println("Enter your PIN : ");
		String userPin=sc.nextLine();
		
		if(!currentUser.getUserPin().equals(userPin)) {
			System.out.println("\n !! You have entered the Wrong PIN \n");
			return;
		}
		
		if(currentUser.getBalance()<amount) {
			System.out.println("\n !! Insufficient balance. \n");
			return;
		}
		
		currentUser.setBalance(currentUser.getBalance()-amount);
		
		userRepo.updateUserBalance(currentUser.getUserId(), currentUser.getBalance());
		
		Transaction transaction=new Transaction();
		transaction.setUserId(currentUser.getUserId());
		transaction.setAmount(amount);
		transaction.setTransactionType("Withdraw");
		
		userRepo.addTransaction(transaction);
		System.out.println("\n Money Withdrawl Successfull..!!");
		System.out.println("Updated Balance : "+currentUser.getBalance()+"\n");
	}
	
	private void fundTransfer() {
		if(currentUser==null) {
			System.out.println("Please login first.");
			return;
		}
		
		Scanner sc=new Scanner(System.in);
		boolean exit = false;

        while (!exit) {
            System.out.println("Please choose an option:");
            System.out.println("1. By UPI");
            System.out.println("2. By Account Number");
            System.out.println("3. Exit");

            int choice = sc.nextInt();sc.nextLine();
            
            

            switch (choice) {
                case 1:
                    System.out.println("You chose UPI.");
                    // UPI based transaction Section 
            		System.out.println("Enter recipient's UPI Id  : ");
            		String recipientUpiId=sc.nextLine();

            		System.out.println("Enter amount to transfer : ");
            		double amount=sc.nextDouble();sc.nextLine();
            		

            		System.out.println("Enter your PIN : ");
            		String userPin=sc.nextLine();
            		
            		if(!currentUser.getUserPin().equals(userPin)) {
            			System.out.println("\n !! You have entered the Wrong PIN \n");
            			return;
            		}
            		
            		if(currentUser.getBalance()<amount) {
            			System.out.println("Insufficient balance.");
            			return;
            		}
            		
            		User recipientUser=userRepo.getUserByUpi(recipientUpiId);
		     		transferMoneyToUser(recipientUser, amount);
            		if(recipientUser!=null)
            		System.out.println("Money transferred Successfully...!");
            		exit = true;
            		
                    break;
                case 2:
                	  System.out.println("You chose Account no.");
                      // UPI based transaction Section 
              		System.out.println("Enter recipient's Account Number  : ");
              		String recipientAccountno=sc.nextLine();

              		System.out.println("Enter amount to transfer : ");
              		double amount1=sc.nextDouble();sc.nextLine();
              		
              		System.out.println("Enter your PIN : ");
            		String userPin1=sc.nextLine();
            		
            		if(!currentUser.getUserPin().equals(userPin1)) {
            			System.out.println("\n !! You have entered the Wrong PIN \n");
            			return;
            		}
              		
              		if(currentUser.getBalance()<amount1) {
              			System.out.println("Insufficient balance.");
              			return;
              		}
              		
              		User recipientUser1=userRepo.getUserByAccountNo(recipientAccountno);
			     	transferMoneyToUser(recipientUser1, amount1);
					if(recipientUser1!=null)
              		System.out.println("Money transferred Successfully ...!");
              		exit = true;
                    break;
                case 3:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

	}

	private void transferMoneyToUser(User recipientUser, double amount) {
		if(recipientUser==null) {
			System.out.println("Recipient User not found");
			return;
		}
		
		if(recipientUser.getUserId()==currentUser.getUserId()) {
			System.out.println("Fund transer not allowed to self account...Please try any other..!");
			return;
		}
		
		currentUser.setBalance(currentUser.getBalance()-amount);
		userRepo.updateUserBalance(currentUser.getUserId(), currentUser.getBalance());
		
		recipientUser.setBalance(recipientUser.getBalance()+amount);
		userRepo.updateUserBalance(recipientUser.getUserId(), recipientUser.getBalance());
		
		Transaction transaction=new Transaction();
		transaction.setUserId(currentUser.getUserId());
		transaction.setAmount(amount);
		transaction.setTransactionType("Transfer to user "+recipientUser.getUpiId());
		userRepo.addTransaction(transaction);
		
		Transaction recipientTransaction=new Transaction();
		recipientTransaction.setUserId(recipientUser.getUserId());
		recipientTransaction.setAmount(amount);
		recipientTransaction.setTransactionType("Received from user "+currentUser.getUpiId());
		userRepo.addTransaction(recipientTransaction);
	}
	
	
	public void printTransactionHistory() {
		if(currentUser==null) {
			System.out.println("Please login first.");
			return;
		}
		List<Transaction> transactions=userRepo.getTransactionHistory(currentUser.getUserId());
		if(transactions.size()==0) {
			System.out.println("No Transaction Found..\n");
			return;
		}
		
		
		System.out.println("Transaction History : ");
		
		for(Transaction t:transactions) {
			System.out.println(t.getTransTimestamp()+" -- "+t.getTransactionType()+" : "+t.getAmount());
		}
	}
	
	
	
    public static void main( String[] args )
    {
    	App app=new App();
		
    	ApplicationContext context = new ClassPathXmlApplicationContext("context.xml");
    	System.out.println("*********  Welcome to Payment Management System  *********      \n");
    	app.showMenu();        
        
    }
}
	
