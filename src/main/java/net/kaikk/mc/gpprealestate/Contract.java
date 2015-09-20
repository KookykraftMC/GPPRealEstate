package net.kaikk.mc.gpprealestate;

import java.sql.Timestamp;
import java.util.Date;

public class Contract {
	
	public String Owner;				// The owner's UUID (The person leasing the claim)
	public String Landlord;				// The landlord's UUID (The actual Owner)
	public Double Price;				// The price of the lease, and the payment for each renewal.
	public Timestamp NextRenewal;		// The timestamp for the next renewal.
	public Timestamp LastRenewal;		// The timestamp for the last renewal.
	
	public Contract(String landlord, String owner, Date renewal){
		this.Owner = owner;			
		this.Landlord = landlord;	
		
		Date date = new Date();		// Getting the Date
		
		this.LastRenewal = new Timestamp(date.getTime());
		this.NextRenewal = new Timestamp(renewal.getTime());
	}
	
	
	
}
