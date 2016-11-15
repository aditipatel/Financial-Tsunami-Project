import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FinancialTsunami {
	
	public static final String EOD = "~"; //End of data signal

	public static void main(String[] args) {

		JFileChooser filechooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES (*.txt)", "txt", "text");//only allow .txt files
		filechooser.setFileFilter(filter);

		if (filechooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

			java.io.File file = filechooser.getSelectedFile();// Get the selected file

			Scanner input;// Create a Scanner for the file

			try {
				input = new Scanner(file);

				if (file.length() == 0) {
					JOptionPane.showMessageDialog(null, "The file is empty");
				}// end of if

				
				int dataSetCount = 1;
				while(input.hasNextLine()) {
					String line = input.nextLine(); 
					 
					if(EOD.equals(line)) //check for end of data signal indicator
						break;
					else if(line.isEmpty())//end of the data set. skip this blank line and continue
						 continue;
					else{ //process the bank data set
						try{	
							String[] noOfBanksAndSafeLimit= line.split(" ");
							
							int noOfBanks = Integer.parseInt(noOfBanksAndSafeLimit[0]);
							double safeLimit = Double.parseDouble(noOfBanksAndSafeLimit[1]);					

							if(noOfBanks > 0){
								double[][] lenderBorrower = new double[noOfBanks][noOfBanks];
								boolean isIncompleteData = false;
								for(int i=0;i<noOfBanks;i++){
									String bankDetailsStr = input.nextLine();
									if(EOD.equals(bankDetailsStr) || bankDetailsStr.isEmpty()){
										JOptionPane.showMessageDialog(null, "Incomplete data for set "+dataSetCount);
										isIncompleteData = true;
										break;
									}

									String[] bankDetails = bankDetailsStr.split(" ");
									lenderBorrower[i][i] = Double.parseDouble(bankDetails[0]);
									int totalNoOfBorrowers = Integer.parseInt(bankDetails[1]);
									if(totalNoOfBorrowers > 0){
										for(int k=2;k<=(totalNoOfBorrowers*2)+1;k++){
											lenderBorrower[i][Integer.parseInt(bankDetails[k])] = Double.parseDouble(bankDetails[++k]);
										}//end of inner for
									}//end of if(totalNoOfBorrowers > 0)
								}//end of outer for

								if(isIncompleteData){
									dataSetCount++;
									continue;
								}

								List<Integer> unsafeBanks = new ArrayList<Integer>();
								checkForUnsafeBanks(lenderBorrower,noOfBanks,unsafeBanks,safeLimit);
								if(!unsafeBanks.isEmpty())
									JOptionPane.showMessageDialog(null, "Unsafe Banks for set "+dataSetCount+" : "+unsafeBanks.toString());	
								else
									JOptionPane.showMessageDialog(null, "No unsafe banks found for set "+dataSetCount);

								dataSetCount++;

							}//end of if(noOfBanks > 0)	
						}catch(NumberFormatException e){
							JOptionPane.showMessageDialog(null, "Invalid data for set "+dataSetCount++);
							//skip the entire set, since it contains invalid data
							line = input.nextLine();
							while(!line.isEmpty())
								line = input.nextLine();
						}
					}//end of else
				}//end of if (input.hasNextLine())

				input.close();			

			}// end of try

			catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(null, "File does not exist");
			}
		}
	}	
	
	/**
	 * This function recursively evaluates all banks for their safeness based on the safe threshold limit.
	 * @param lenderBorrower
	 * @param checkNoOfBanks
	 * @param unsafeBankIds
	 * @param safeLimit
	 */
	private static void checkForUnsafeBanks(double[][] lenderBorrower,int checkNoOfBanks,List<Integer> unsafeBankIds,double safeLimit){

		for(int i=0;i<checkNoOfBanks;i++){
			if(null != unsafeBankIds && !unsafeBankIds.contains(i)){
				double totalAsset = 0;
				for(int j=0;j<lenderBorrower[i].length;j++){
					if(null != unsafeBankIds && !unsafeBankIds.contains(j)){
						totalAsset += lenderBorrower[i][j];
					}//end of if(null != unsafeBankIds && !unsafeBankIds.contains(j)) 				
				}//end of inner for loop
				if(totalAsset < safeLimit){
					unsafeBankIds.add(i);
					if( i > 0)
						checkForUnsafeBanks(lenderBorrower,i,unsafeBankIds,safeLimit);
				}//end of if(totalAsset < safeLimit)
			}//end of outermost if
		}//end of outer for loop
	}
	
}
 