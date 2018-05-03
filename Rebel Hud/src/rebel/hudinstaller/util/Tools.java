package rebel.hudinstaller.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;

import rebel.hudinstaller.lib.Constants;
import rebel.hudinstaller.ui.HudUI;

/**
 * Contains methods needed to actually download the HUD
 */

public class Tools
{
    private static String strInstallPath = "";
    private static File zipPath;
    private static URL urlHUD;

    static
    {
        try
        {
            urlHUD = new URL(Constants.GIT_URL);        // URL to download the HUD from
        } catch(MalformedURLException e)
        {
            e.printStackTrace();
        }
    }

    //Public Methods

    /**
     * Installs the HUD
     *
     * @throws Throwable Throws an error if the HUD could not download
     */
    public static void installHud() throws Throwable
    {
        checkOS();                  // Checks what the OS is
        if(checkInstalled() == 1)   // Checks if the HUD is installed
        {
            throw new Throwable("The HUD is already installed", new Throwable("Installation Error"));
        }
        downloadZip();              // Downloads the HUD as a ZIP file
        extractZip();               // Extracts the ZIP file to a temporary folder
        copyHud();                  // Copies the required files out of the temporary folder
        cleanUp();                  // Deletes the ZIP download and temporary files
    }

    /**
     * Installs the HUD if it is being updated
     *
     * @throws Throwable Throws an error if the HUD could not download
     */
    public static void installHudUpdating() throws Throwable
    {
        checkOS();      // Checks what the OS is
        downloadZip();  // Downloads the HUD as a ZIP file
        extractZip();   // Extracts the ZIP file to a temporary folder
        copyHud();      // Copies the required files out of the temporary folder
        cleanUp();      // Deletes the ZIP download and temporary files
    }

    /**
     * Checks if there is an update available to the HUD
     *
     * @return Returns 1 if the user wants to update, 0 if they do not
     * @throws Throwable Throws an error if the HUD is not installed
     */
    public static int updateHud() throws Throwable
    {
        checkOS();                  // Checks what the OS is
        if(checkInstalled() == 1)   // Checks if the HUD is installed
            return checkVersion();  // Checks if an update is available
        else
            throw new Throwable("The HUD is not currently installed", new Throwable("Update Error"));
    }

    /**
     * Checks if there is an update to the installer
     *
     * @return Returns 1 if the user wants to update, 0 if they do not
     */
    public static int updateInstaller()
    {
        return checkInstallerVersion(); // Checks what the installer version is
    }

    /**
     * Removes the installed HUD from the system
     *
     * @throws Throwable Throws an error if the HUD is not installed
     */
    public static void removeHud() throws Throwable
    {
        checkOS();
        if(checkInstalled() == 1)
            delete(new File(strInstallPath + "RebelHud/"));
        else
            throw new Throwable("The HUD is not currently installed", new Throwable("Removal Error"));
    }

    /**
     * Deletes the ZIP download and the temporary folder
     */
    public static void cleanUp()
    {
        delete(new File(strInstallPath + "temp/"));
        delete(new File(strInstallPath + "dl.zip"));
    }


    // Private Methods

    /**
     * Checks if the HUD is currently installed
     *
     * @param none
     * @return Returns 1 if the HUD is installed
     * @throws Throws an error if the HUD is not installed
     * @pre none
     * @post none
     */
    private static int checkInstalled() throws Throwable
    {
        File folder = new File(strInstallPath + "RebelHud/version.txt");
        if(!folder.exists())        // If the file does not exist
        {
            //throw new Throwable("The HUD is not currently installed", new Throwable("Update Error"));
            return 0;
        }
        else
        {
            return 1;
        }
    }

    /**
     * Checks what the operation system the user has
     *
     * @param none
     * @return Returns 1 if an operation system is found
     * @throws Throwable Throws an error if it is unknown
     * @pre none
     * @post The install path is set
     */
    private static int checkOS() throws Throwable
    {
        int count = 0;
        while(true)
        {
            if(count == Constants.tf2Locations.length)
                break;
            File folder = new File(Constants.tf2Locations[count]);        // Sets the file to Mac OSX path
            if(folder.exists())                                        // If the folder exists
            {
                zipPath = new File(Constants.tf2Locations[count] + "dl.zip");
                strInstallPath = Constants.tf2Locations[count];
                return 1;
            }
            count++;
        }

        throw new Throwable("TF2 could not be found", new Throwable("TF2 Not Installed"));
    }

    /**
     * Checks if any updates are available for the HUD
     *
     * @param none
     * @return Returns 1 if the user wants to update, 0 if they do not
     * @throws Throws an error if the HUD is not installed
     * @pre The HUD is installed
     * @post none
     */
    private static int checkVersion() throws Throwable
    {
        try
        {
            Scanner in;
            int choice;
            String input1;
            String input2;
            double currentVersion;        // Version of the HUD currently installed
            double latestVersion;        // Latest version of the HUD

            in = new Scanner(
                    new URL(Constants.LATEST_HUD).openStream());        // Opens connection to online version.txt
            input1 = in.next();
            latestVersion = toDouble(input1);        // Reads in the latest version
            in.close();

            in = new Scanner(new File(strInstallPath + "RebelHud/version.txt"));        // Opens local version.txt
            input2 = in.next();
            currentVersion = toDouble(input2);        // Reads in the current version
            in.close();

            if(latestVersion > currentVersion)        // If there is a newer version
            {
                String strUpdateMsg = "There are updates availible.\n"
                        + "Current version: " + input2 + "\n"
                        + "Latest Version: " + input1 + "\n"
                        + "Would you like to install the update?";
                Object[] options = {"Yes", "No"};
                choice = JOptionPane
                        .showOptionDialog(null, strUpdateMsg, "Availible Updates", JOptionPane.YES_NO_OPTION,
                                          JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                if(choice == 0)        // If the user wants to update
                {
                    return 1;
                }
                else                                // If the user does not want to update
                {
                    return 0;
                }
            }
            else        // If there is not a newer version
            {
                String strUpdateMsg = "There are no updates availible.\n";
                @SuppressWarnings("unused")
                Object[] options = {"Yes", "No"};
                JOptionPane.showMessageDialog(null, strUpdateMsg, "Availible Updates", JOptionPane.INFORMATION_MESSAGE);
                return 0;
            }
        } catch(FileNotFoundException e)
        {
            throw new Throwable("The HUD is not currently installed", new Throwable("Update Error"));
        }
    }

    /**
     * Checks if any updates are available for the installer
     *
     * @param none
     * @return Returns 1 if the user wants to update the installer
     * @pre none
     * @post none
     */
    private static int checkInstallerVersion()
    {
        int answer = -1;
        String path = HudUI.class.getProtectionDomain().getCodeSource().getLocation()
                .getPath();        // Path of the running JAR file
        try
        {
            Scanner in = new Scanner(
                    new URL(Constants.LATEST_INSTALLER).openStream());        // Opens connection to the version.txt
            double inputVersion = in.nextDouble();        // Reads in the current version
            in.close();
            if(inputVersion > Constants.INSTALLER_VERSION)        // If there is a newer version
            {
                answer =
                        JOptionPane.showConfirmDialog(null, "A new version of this"
                                                              + " installer is available\nWould you like to install it?",
                                                      "Installer Update", JOptionPane.YES_NO_OPTION,
                                                      JOptionPane.INFORMATION_MESSAGE);
            }

            if(answer == 0)        // If the user wants to update
            {
                Tools.downloadInstaller(URLDecoder.decode(path, "UTF-8"));
                JOptionPane.showMessageDialog(null,
                                              "The update was completed successfuly.\n"
                                                      + "Please restart the application in order for the changes take effect.");
                return 1;
            }
            else        // If the user does not want to update
            {
                return 0;
            }
        } catch(IOException e)
        {
            JOptionPane.showMessageDialog(null, "ERROR: " + "Cannot Check Version Info", "ERROR",
                                          JOptionPane.ERROR_MESSAGE);
            return 0;
        }
    }

    /**
     * Downloads the new version of the installer
     *
     * @param Path The path of the current installer
     * @return none
     * @pre none
     * @post none
     */
    private static void downloadInstaller(String path)
    {
        File installerPath = new File(path);
        try
        {
            URL urlInstaller = new URL(Constants.INSTALLER_URL);        // URL of the JAR download
            dl(installerPath, urlInstaller);        // Downloads the JAR file
        } catch(Throwable e)
        {
            JOptionPane
                    .showMessageDialog(null, "ERROR: " + e.getMessage(), "Download error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Downloads the ZIP of the HUD
     *
     * @param none
     * @return none
     * @throws Throws a download error if it cannot download
     * @pre none
     * @post none
     */
    private static void downloadZip() throws Throwable
    {
        try
        {
            dl(zipPath, urlHUD);        // Downloads the HUD
        } catch(IOException e)
        {
            throw new Throwable("The HUD could not download", new Throwable("Download error"));
        }
    }

    /**
     * Extracts the ZIP file to a temporary folder
     *
     * @param none
     * @return none
     * @throws Throws an error if it cannot extract
     * @pre The ZIP file is downloaded
     * @post The files are extracted from the ZIP
     */
    private static void extractZip() throws Throwable
    {
        try
        {
            unZip(zipPath, new File(strInstallPath + "temp/"));        // Unzips the ZIP file
        } catch(IOException e)
        {
            throw new Throwable("The HUD could not be installed.\nPlease exit TF2.", new Throwable("File Error"));
        }
    }

    /**
     * Copies the folders from the temporary folder to the HUD installation path
     *
     * @param none
     * @return none
     * @throws Throws an error if the files cannot copy
     * @pre none
     * @post none
     */
    private static void copyHud() throws Throwable
    {
        try
        {
            FileUtils
                    .copyDirectory(new File(strInstallPath + "temp/RebelHud-Master/custom/"), new File(strInstallPath));
        } catch(IOException e)
        {
            throw new Throwable("The HUD could not be installed", new Throwable("File Error"));
        }
    }

    /**
     * Unzips a ZIP file
     *
     * @param zipPath  The location where the ZIP was downloaded to
     * @param destpath The location where the files will be extracted to
     * @return none
     * @throws Throws an error if the ZIP cannot be extracted
     * @pre none
     * @post none
     */
    private static void unZip(File zipPath, File destPath) throws IOException
    {
        if(!destPath.exists())
        {
            destPath.mkdir();
        }
        ZipInputStream in = new ZipInputStream(
                new FileInputStream(zipPath));        // Input stream to read from the ZIP
        ZipEntry entry = in
                .getNextEntry();                                                                                // The entry from the ZIP file
        while(entry != null)        // While there are still entries
        {
            String filePath = destPath + File.separator + entry.getName();
            if(!entry.isDirectory())
            {
                extract(in, filePath);
            }
            else
            {
                File dir = new File(filePath);
                dir.mkdir();
            }
            in.closeEntry();
            entry = in.getNextEntry();
        }
        in.close();
    }

    /**
     * Extracts the ZIP file
     *
     * @param filePath Where to extract the files to
     * @param in       Input stream of the file
     * @return none
     * @pre none
     * @post none
     */
    private static void extract(ZipInputStream in, String filePath) throws IOException
    {
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytes = new byte[4096];
        int read = 0;
        while((read = in.read(bytes)) != -1)
        {
            out.write(bytes, 0, read);
        }
        out.close();
    }

    /**
     * Downloads a ZIP file
     *
     * @param file The file that will be downloaded
     * @param url  The URL of the file to download
     * @return none
     * @throws Throws an error if the file cannot download
     * @pre none
     * @post none
     */
    private static void dl(File file, URL url) throws Throwable
    {
        if(file != null)
        {
            InputStream in = null;
            OutputStream out = null;
            in = url.openStream();
            out = new FileOutputStream(file);
            // Begin transfer
            transfer(in, out);
            in.close();
            out.close();
        }
    }

    /**
     * Deletes a file
     *
     * @param file The file to be deleted
     * @return none
     * @pre The file exists
     * @post The file is deleted
     */
    private static void delete(File file)
    {
        if(file.isDirectory())
        {
            if(file.list().length == 0)
            {
                file.delete();
            }
            else
            {
                String files[] = file.list();
                for(String path : files)
                {
                    File deleteFile = new File(file, path);
                    delete(deleteFile);
                }
                if(file.list().length == 0)
                {
                    file.delete();
                }
            }
        }
        else
        {
            file.delete();
        }
    }

    /**
     * Transfers the download from the instream to a folder
     *
     * @param in  Stream to read from
     * @param out Stream to write to
     * @return none
     * @pre none
     * @post none
     */
    private static void transfer(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[2048];
        int bytesRead;
        while((bytesRead = in.read(buffer)) > 0)
        {
            out.write(buffer, 0, bytesRead);
        }
    }

    /**
     * Takes a string and converts to a double
     *
     * @param text Version number as a String
     * @return Double
     */
    private static double toDouble(String str)
    {
        int firstDecimal = str.indexOf('.');
        String temp = str;
        if(firstDecimal != -1)
        {
            String firstHalf = str.substring(0, firstDecimal + 1);
            String secondHalf = str.substring(firstDecimal + 1);
            temp = firstHalf + secondHalf.replaceAll("\\.", "");
        }
        return new Double(temp).doubleValue();
    }
}
