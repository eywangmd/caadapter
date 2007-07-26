package gov.nih.nci.caadapter.ui.mapping.sdtm;

import gov.nih.nci.caadapter.common.csv.CSVDataResult;
import gov.nih.nci.caadapter.common.csv.data.CSVField;
import gov.nih.nci.caadapter.common.csv.data.CSVSegment;
import gov.nih.nci.caadapter.common.csv.data.CSVSegmentedFile;
import gov.nih.nci.caadapter.common.csv.meta.CSVMeta;
import gov.nih.nci.caadapter.common.util.UUIDGenerator;
import gov.nih.nci.caadapter.sdtm.util.CSVMapFileReader;
import gov.nih.nci.caadapter.ui.mapping.sdtm.RDSFixedLenghtInput;
import gov.nih.nci.caadapter.ui.mapping.sdtm.RDSHelper;
import gov.nih.nci.caadapter.ui.mapping.sdtm.RDSMapResult;
import gov.nih.nci.caadapter.ui.common.AbstractMainFrame;
import gov.nih.nci.caadapter.ui.mapping.sdtm.SDTMMany2ManyMapping;
import gov.nih.nci.caadapter.ui.specification.csv.CSVPanel;
import gov.nih.nci.caadapter.ui.main.MainMenuBar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: hjayanna
 * Date: Jun 22, 2007
 * Time: 8:31:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class RDSTransformer
{

    String directoryLocation;

    Hashtable globaldomainList;

    Hashtable hashTableTransform;

    CSVDataResult csvDataResult;

    LinkedHashMap mappedSegmentRecords;

    Hashtable finalResultList = new Hashtable();

    ArrayList removeList;

    HashMap fixedLengthRecords;

    //HashMap prefs;

    boolean fixedLengthIndicator = false;

    public RDSTransformer(AbstractMainFrame callingFrame, File mapFile, String _csvFileName, String _directoryLocation) throws Exception
    {
        directoryLocation = _directoryLocation;
       
        CSVMapFileReader csvMapFileReader = new CSVMapFileReader(mapFile);
        //check for fixed lenght
        if (((String) MainMenuBar.getCaAdapterPreferences().get("FIXED_LENGTH_VAR")).equalsIgnoreCase("Fixed"))
        {
            fixedLengthIndicator = true;
            //Prepare the list here and keep it ready so that number of blanks corresponding to the
            //value set by the user will be applied appropriately
            RDSFixedLenghtInput rdsFixedLenghtInput = new RDSFixedLenghtInput(callingFrame, csvMapFileReader.getTargetKeyList());
            fixedLengthRecords = rdsFixedLenghtInput.getUserValues();
        }
        globaldomainList = RDSHelper.getAllFieldsForDomains(new File(RDSHelper.getDefineXMLNameFromMapFile(mapFile.getAbsolutePath())));
        hashTableTransform = csvMapFileReader.getHashTableTransform();
        String scsFileName = RDSHelper.getSCSFileFromMapFile(mapFile);
        prepareCSVDataFromCSVDataFile(_csvFileName, scsFileName);
    }

    private void prepareCSVDataFromCSVDataFile(String _csvFileName, String _scsFileName)
    {
        CSVPanel csvPanel = new CSVPanel();
        File csvFile = new File(_csvFileName);
        csvPanel.setSaveFile(new File(_scsFileName), true);
        CSVMeta rootMeta = csvPanel.getCSVMeta(false);
        SDTMMany2ManyMapping segmentedCSVParser = new SDTMMany2ManyMapping();
        csvDataResult = segmentedCSVParser.parse(csvFile, rootMeta);
        List csvDATA = segmentedCSVParser.returnCsvMapData1;
        csvDATA.add(csvDATA.get(0));
        processDataRecords();
    }

    private void processDataRecords()
    {
        CSVSegmentedFile csvSegmentedFile = csvDataResult.getCsvSegmentedFile();
        List recordsinCSVFile = csvSegmentedFile.getLogicalRecords();
        for (int k = 0; k < recordsinCSVFile.size(); k++)
        {
            processOneRecord(recordsinCSVFile, k);
        }
    }

    public HashMap getFixedLengthRecords()
    {
        return fixedLengthRecords;
    }

    private void processOneRecord(List records, int recordNumber)
    {
        CSVSegment csvSegment = (CSVSegment) records.get(recordNumber);
        /*
            a.)Now we have all the possible leaf segments!
            b.)The size of the collection is to the number of rows!
            b.)Process each leaf record and this transforms one row in the resulting txt file!
            c.)The 'searchMappedField' method must return a arrayList
            d.)The arraylist is added to the collection and this is written out to the file.
         */
        Enumeration enumerat = hashTableTransform.keys();
        while (enumerat.hasMoreElements())
        {
            String domainName = (String) enumerat.nextElement();
            mappedSegmentRecords = new LinkedHashMap();
            prepareMappedSegments(csvSegment, domainName);
            prepareRemoveList(domainName);
            processResultForDomain(domainName);
        }
        //finally! write the table out
        Enumeration enume = finalResultList.keys();
        while (enume.hasMoreElements())
        {
            String domainName = (String) enume.nextElement();
            writeFile(domainName, (ArrayList) finalResultList.get(domainName));
        }
    }

    private void prepareRemoveList(String domainName)
    {
        removeList = new ArrayList();
        Set set = mappedSegmentRecords.keySet();
        Iterator iter = set.iterator();
        while (iter.hasNext())
        {
            String uuID = (String) iter.next();
            CSVSegment csvSegment = (CSVSegment) mappedSegmentRecords.get(uuID);
            if (areChildrenSegmentsMapped(csvSegment, domainName))
            {
                removeList.add(uuID);
            }
        }
    }

    private boolean areChildrenSegmentsMapped(CSVSegment csvSegment, String domainName)
    {
        boolean retVal = false;
        if (csvSegment.getChildSegments().size() > 0)
        {
            List list = csvSegment.getChildSegments();
            for (int i = 0; i < list.size(); i++)
            {
                CSVSegment csvChildSegment = (CSVSegment) list.get(i);
                if (isSegmentMapped(csvChildSegment, domainName))
                {
                    return true;
                }
                areChildrenSegmentsMapped(csvChildSegment, domainName);
            }
        }
        return retVal;
    }

    private void prepareMappedSegments(CSVSegment csvSegment, String domainName)
    {
        if (isSegmentMapped(csvSegment, domainName))
        {
            //mappedSegmentRecords.add(csvSegment);
            mappedSegmentRecords.put(UUIDGenerator.getUniqueString(), csvSegment);
        }
        //parent check for this domain only
        checkForMappedParents(csvSegment, domainName);
        //children check for this domain only
        checkForMappedChildren(csvSegment, domainName);
    }

    private void checkForMappedParents(CSVSegment csvSegment, String domainName)
    {
        if (csvSegment.getParentSegment() != null)
        {
            CSVSegment csvParentSegment = csvSegment.getParentSegment();
            if (isSegmentMapped(csvParentSegment, domainName))
            {
                mappedSegmentRecords.put(UUIDGenerator.getUniqueString(), csvParentSegment);
            }
            checkForMappedParents(csvParentSegment, domainName);
        }
    }

    private void checkForMappedChildren(CSVSegment csvSegment, String domainName)
    {
        if (csvSegment.getChildSegments().size() > 0)
        {
            List list = csvSegment.getChildSegments();
            for (int i = 0; i < list.size(); i++)
            {
                CSVSegment csvChildSegment = (CSVSegment) list.get(i);
                if (isSegmentMapped(csvChildSegment, domainName))
                {
                    mappedSegmentRecords.put(UUIDGenerator.getUniqueString(), csvChildSegment);
                }
                checkForMappedChildren(csvChildSegment, domainName);
            }
        }
    }

    private void processResultForDomain(String domainName)
    {
        Set set = mappedSegmentRecords.keySet();
        Iterator iter = set.iterator();
        while (iter.hasNext())
        {
            String _UUID = (String) iter.next();
            if (!doesRemoveListHaveUUID(_UUID))
            {
                processResultForDomain2((CSVSegment) mappedSegmentRecords.get(_UUID), domainName);
            }
        }
    }

    private boolean doesRemoveListHaveUUID(String uuid)
    {
        boolean retVal = false;
        for (int k = 0; k < removeList.size(); k++)
        {
            if (uuid.equals(removeList.get(k)))
                return true;
        }
        return retVal;
    }

    private void processResultForDomain2(CSVSegment csvSegment, String domainName)
    {
        ArrayList rowData;
        rowData = new ArrayList();
        int sizeOfDomain = new Integer(((ArrayList) globaldomainList.get(domainName)).size());
        for (int l = 0; l < sizeOfDomain; l++)
        {
            rowData.add(" ");//an empty space buffer pre-added for data assumed to be mapped
        }
        searchMappedField(csvSegment, rowData, domainName);
        //writeFile(domainName, rowData);
        if (finalResultList.containsKey(domainName))
        {
            ((ArrayList) finalResultList.get(domainName)).add(rowData);
        } else
        {
            ArrayList resArray = new ArrayList();
            resArray.add(rowData);
            finalResultList.put(domainName, resArray);
        }
    }

    private void searchMappedField(CSVSegment csvSegment, ArrayList rowData, String domainName)
    {
        if (!isSegmentMapped(csvSegment, rowData, domainName))
        {
            checkParentSegment(csvSegment, rowData, domainName);
        }
    }

    private void checkParentSegment(CSVSegment csvSegment, ArrayList rowData, String domainName)
    {
        if (csvSegment.getParentSegment() != null)
        {
            csvSegment = csvSegment.getParentSegment();
            searchMappedField(csvSegment, rowData, domainName);
            checkParentSegment(csvSegment, rowData, domainName);
        }
    }

    private boolean isSegmentMapped(CSVSegment csvSegment, ArrayList rowData, String domainName)
    {
        boolean retVal = false;
        //This Xpath will be used to prefix all the fields
        String xpathPrefix = RDSHelper.getParentasXPath(csvSegment);
        if (csvSegment.getFields().size() > 0)
        {
            List fieldsForEachSegment = csvSegment.getFields();
            for (int fields = 0; fields < fieldsForEachSegment.size(); fields++)
            {
                CSVField fieldObject = (CSVField) fieldsForEachSegment.get(fields);
                String mappedValue = isFieldMapped(xpathPrefix + "\\" + fieldObject.getMetaObject().toString());
                if (mappedValue != null)// okay what about parent segments are they mapped; check that in the else clause
                {
                    if (domainName.equalsIgnoreCase(mappedValue.substring(0, 2)))
                    {
                        //A simple check to continue for only matching domains and fields
                        if (!fixedLengthIndicator)
                        {
                            fixedLengthRecords = null;
                        }
                        new RDSMapResult(domainName, globaldomainList, mappedValue, xpathPrefix + "\\" + fieldObject.getMetaObject().toString(), fieldObject.getValue(), csvSegment, hashTableTransform, rowData, fixedLengthRecords).getArrayList();
                        retVal = true;
                    }
                }
            }
        }
        return retVal;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean isSegmentMapped(CSVSegment csvSegment, String domainName)
    {
        boolean retVal = false;
        //This Xpath will be used to prefix all the fields
        String xpathPrefix = RDSHelper.getParentasXPath(csvSegment);
        if (csvSegment.getFields().size() > 0)
        {
            List fieldsForEachSegment = csvSegment.getFields();
            for (int fields = 0; fields < fieldsForEachSegment.size(); fields++)
            {
                CSVField fieldObject = (CSVField) fieldsForEachSegment.get(fields);
                String mappedValue = isFieldMapped(xpathPrefix + "\\" + fieldObject.getMetaObject().toString());
                if (mappedValue != null)// okay what about parent segments are they mapped; check that in the else clause
                {
                    if (domainName.equalsIgnoreCase(mappedValue.substring(0, 2)))
                    {
                        retVal = true;
                    }
                }
            }
        }
        return retVal;
    }

    private String isFieldMapped(String field)
    {
        Enumeration enum1 = hashTableTransform.keys();
        while (enum1.hasMoreElements())
        {
            String domainName = (String) enum1.nextElement();
            ArrayList array = (ArrayList) hashTableTransform.get(domainName);
            for (int i = 0; i < array.size(); i++)
            {
                String _tempStr = array.get(i).toString();
                String _temStr2 = _tempStr.substring(0, _tempStr.indexOf("~"));
                if (_temStr2.equals(field))
                {
                    return _tempStr.substring(_tempStr.indexOf("~") + 1, _tempStr.length());
                }
            }
        }
        return null;
    }

    private void writeFile(String domainName, ArrayList resultData)
    {
        try
        {
            ArrayList domainHeader = (ArrayList) globaldomainList.get(domainName);
            //create a file with domain name
            FileWriter fstream = new FileWriter(directoryLocation + "\\" + domainName + ".csv");
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(domainHeader.toString().substring(1, domainHeader.toString().indexOf(']')));
            for (int i = 0; i < resultData.size(); i++)
            {
                try
                {
                    ArrayList writeToList = (ArrayList) resultData.get(i);
                    if (checkIfValueExists(writeToList))
                    {
                        String tempStr = writeToList.toString();
                        String resultStr = tempStr.substring(1, tempStr.indexOf(']'));
                        out.write("\n" + resultStr);
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();//To change body of catch statement use File | Settings | File Templates.
                    System.out.println("continuing to next loop " + e.getMessage());
                }
            }
            out.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private boolean checkIfValueExists(ArrayList rowData)
    {
        boolean retVal = false;
        for (int i = 0; i < rowData.size(); i++)
        {
            if (rowData.get(i).toString().length() > 1)
                return true;
        }
        return retVal;
    }
}