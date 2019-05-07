package android.com.identificatecadministrador;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MifareControl extends AppCompatActivity {

    private static final String TAG = "Mifare_control";
    private static final String KEYA = "FFFFFFFFFFFF";
    private static final String KEYB = "FFFFFFFFFFFF";

    // NFC-related variables
    NfcAdapter mNfcAdapter;
    PendingIntent mNfcPendingIntent;
    IntentFilter[] mReadWriteTagFilters;
    private String mode;
    String[][]mTechList;
    AlertDialog mTagDialog;

    private int writeInitialBlock;
    private ArrayList<Integer> writeIntValues = new ArrayList<>();
    private ArrayList<String> writeDataBlocks = new ArrayList<>();
    private int extraByteABSector;
    private String extraByteABSectorValue;
    private int readInitialBlock;
    private int blocksToRead;
    private ArrayList<String> blocksRead = new ArrayList<>();
    private ArrayList<Integer> valueBlocksRead = new ArrayList<>();

    Converters convert = new Converters();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mifare_control);

        setMode();

        // get an instance of the context's cached NfcAdapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // if null is returned this demo cannot run. Use this check if the
        // "required" parameter of <uses-feature> in the manifest is not set
        if (mNfcAdapter == null)
        {
            Toast.makeText(this,
                    "Su dispositivo no soporta NFC. No se puede correr la aplicación.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // check if NFC is enabled
        checkNfcEnabled();

        // Handle foreground NFC scanning in this activity by creating a
        // PendingIntent with FLAG_ACTIVITY_SINGLE_TOP flag so each new scan
        // is not added to the Back Stack
        mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Create intent filter to handle MIFARE NFC tags detected from inside our
        // application when in "read mode":
        IntentFilter mifareDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        try {
            mifareDetected.addDataType("application/com.e.mifarecontrol");
        } catch (IntentFilter.MalformedMimeTypeException e)
        {
            throw new RuntimeException("No se pudo añadir un tipo MIME.", e);
        }

        // Create intent filter to detect any MIFARE NFC tag when attempting to write
        // to a tag in "write mode"
        mReadWriteTagFilters = new IntentFilter[] { mifareDetected };

        // Setup a tech list for all NfcF tags
        mTechList = new String[][] { new String[] { MifareClassic.class.getName() } };
    }

    void setMode() {
        String title = "";
        String message = "";
        mode = getIntent().getStringExtra("mode");
        Log.i("ModeRec", mode);
        if (mode.equals("readUID")) {
            title = "Listo para leer número de serie";
            message = "Acerca la credencial para leer su número de serie";
        }
        else if(mode.equals("writeManyValueBlocks")) {
            writeIntValues.clear();
            writeIntValues = getIntent().getIntegerArrayListExtra("writeValueBlocks");

            writeInitialBlock = getIntent().getIntExtra("initialBlock", 1);

            title="Listo para escribir muchos bloques de valores";
            message = "Acerca la credencial para escribir muchos bloques de valores";
        }
        else if(mode.equals("writeManyDataBlocks")) {
            writeDataBlocks.clear();
            writeDataBlocks = getIntent().getStringArrayListExtra("writeDataBlocks");

            writeInitialBlock = getIntent().getIntExtra("initialBlock", 1);

            title = "Listo para escribir muchos bloques de datos";
            message = "Acerca la credencial para escribir muchos bloques de datos";

        }
        else if(mode.equals("readExtraByteAB")) {
            extraByteABSector = getIntent().getIntExtra("extraByteABSector", 0);

            title = "Listo para leer byte adicional de bits de acceso";
            message = "Acerca la credencial para leer byte adicional de bits de acceso";
        }
        else if(mode.equals("writeExtraByteAB")) {
            extraByteABSector = getIntent().getIntExtra("extraByteABSector", 0);

            extraByteABSectorValue = getIntent().getStringExtra("extraByteABSectorValue");

            title = "Listo para escribir byte adicional de bits de acceso";
            message = "Acerca la credencial para escribir byte adicional de bits de acceso";
        }
        else if(mode.equals("readManyDataBlocks")) {
            readInitialBlock = getIntent().getIntExtra("readInitialBlock", 0);
            blocksToRead = getIntent().getIntExtra("blocksToRead", 0);

            title = "Listo para leer bloques de datos";
            message = "Acerca la credencial para leer muchos bloques de datos";
        }
        else if(mode.equals("readManyValueBlocks")) {
            readInitialBlock = getIntent().getIntExtra("readInitialBlock", 0);
            blocksToRead = getIntent().getIntExtra("blocksToRead", 0);

            title = "Listo para leer bloques de valor";
            message = "Acerca la credencial para leer muchos bloques de valor";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setNegativeButton("Cancelar",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                                setResult(Activity.RESULT_CANCELED, new Intent());
                                finish();
                            }
                        });
        mTagDialog = builder.create();
        mTagDialog.show();
    }


    void resolveReadUIDIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            byte[] tagUID = tagFromIntent.getId();
            String hexUID = convert.getHexString(tagUID, tagUID.length);
            Log.i("UID", "Tag UID: " + hexUID);

            mTagDialog.cancel();

            Intent i = new Intent();
            i.putExtra("UID", hexUID);
            setResult(Activity.RESULT_OK, i);
            finish();
        }
    }

    void resolveWriteManyBlocksIntent(Intent intent, Boolean areValueBlocks) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareClassic mfc = MifareClassic.get(tagFromIntent);
            try {
                mfc.connect();
                Boolean noErrors = true;
                int blocksWritten = 0;
                int currWriteBlock = writeInitialBlock;
                int writeBlocksIndex = 0;
                int blocksToWrite = areValueBlocks? writeIntValues.size() : writeDataBlocks.size();
                while (blocksWritten < blocksToWrite)  {
                    if ((currWriteBlock+1)%4 == 0) {
                        currWriteBlock++;
                    }

                    if (currWriteBlock == writeInitialBlock || currWriteBlock%4 == 0) {
                        if (!authenticateSector(currWriteBlock, KEYA, true, mfc)) {
                            noErrors = false;
                            break;
                        }
                    }

                    byte[] dataToWrite = areValueBlocks ?
                            intToValueBlock(writeIntValues.get(writeBlocksIndex), currWriteBlock) :
                            convert.hexStringToByteArray(writeDataBlocks.get(writeBlocksIndex));
                    mfc.writeBlock(currWriteBlock, dataToWrite);
                    currWriteBlock++;
                    blocksWritten++;
                    writeBlocksIndex++;
                }
                mfc.close();
                mTagDialog.cancel();

                Intent i = new Intent();
                i.putExtra("writeManyBlocksNoErrors", noErrors);
                setResult(Activity.RESULT_OK, i);
                finish();
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }

    void resolveExtraByteAB(Intent intent, boolean readExtraByte) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareClassic mfc = MifareClassic.get(tagFromIntent);
            try {
                Boolean noErrors = false;
                String byteAB = "";
                mfc.connect();
                if (mfc.authenticateSectorWithKeyA(extraByteABSector, convert.hexStringToByteArray(KEYA))) {
                    if (readExtraByte) {
                        byte[] dataRead = mfc.readBlock(extraByteABSector*4+3);
                        String blockRead = convert.getHexString(dataRead, dataRead.length);
                        byteAB = blockRead.substring(18, 20);
                        Log.i("ByteAB", "Byte adicional de AB de sector: " + extraByteABSector +": " + byteAB);
                    }
                    else {
                        String blockRead = convert.getHexString(mfc.readBlock(extraByteABSector*4+3), 16);
                        byte[] blockToWrite = convert.hexStringToByteArray(
                                KEYA
                                + blockRead.substring(12,18)+extraByteABSectorValue
                                +KEYB);
                        mfc.writeBlock(extraByteABSector*4+3,blockToWrite);
                    }
                    noErrors = true;
                }

                mfc.close();
                mTagDialog.cancel();

                Intent i = new Intent();
                i.putExtra("readExtraByteABNoErrors", noErrors);
                if (readExtraByte) {
                    i.putExtra("extraByteAB", byteAB);
                }
                setResult(Activity.RESULT_OK, i);
                finish();
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }

    void resolveReadManyBlocks(Intent intent, boolean areValueBlocks) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareClassic mfc = MifareClassic.get(tagFromIntent);
            try {
                mfc.connect();
                Boolean noErrors = true;
                if (!areValueBlocks) {
                    for(int curReadBlock=readInitialBlock; curReadBlock<(blocksToRead+readInitialBlock); curReadBlock++) {
                        if (curReadBlock == readInitialBlock || curReadBlock%4 == 0) {
                            if (!authenticateSector(curReadBlock, KEYA, true, mfc)) {
                                noErrors = false;
                                break;
                            }
                        }
                        byte[] b = mfc.readBlock(curReadBlock);
                        blocksRead.add(convert.getHexString(b, b.length));
                        Log.i("Read", convert.getHexString(b, b.length));
                    }
                }
                else {
                    int blocksRead = 0;
                    int currReadBlock = readInitialBlock;
                    while (blocksRead < blocksToRead)  {
                        if ((currReadBlock+1)%4 == 0) {
                            currReadBlock++;
                        }

                        if (currReadBlock == readInitialBlock || currReadBlock%4 == 0) {
                            if (!authenticateSector(currReadBlock, KEYA, true,mfc)) {
                                noErrors = false;
                                break;
                            }
                        }

                        byte[] read =  mfc.readBlock(currReadBlock);
                        int valueOfBlock = ByteBuffer.wrap(new byte[] {read[3], read[2], read[1], read[0]}).getInt();
                        int address = ByteBuffer.wrap(mfc.readBlock(currReadBlock)).get(12);
                        valueBlocksRead.add(valueOfBlock);
                        currReadBlock++;
                        blocksRead++;
                    }
                }
                mfc.close();
                mTagDialog.cancel();

                Intent i = new Intent();
                i.putExtra("readManyBlocksNoErrors", noErrors);
                if (!areValueBlocks) {
                    i.putExtra("blocksType", "data");
                    i.putExtra("blocksRead", blocksRead);
                }
                else {
                    i.putExtra("blocksType", "value");
                    i.putExtra("blocksRead", valueBlocksRead);
                }
                setResult(Activity.RESULT_OK, i);
                finish();
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }

    boolean authenticateSector(int block, String hexkey, Boolean isKeyA, MifareClassic mfc) {
        boolean auth = false;
        try {
            byte[] datakey = convert.hexStringToByteArray(hexkey);
            int sector = mfc.blockToSector(block);
            if(isKeyA) {
                auth= mfc.authenticateSectorWithKeyA(sector, datakey);
            }
            else {
                auth= mfc.authenticateSectorWithKeyB(sector, datakey);
            }
        }
        catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        return auth;
    }


    @Override
    public void onPause()
    {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        checkNfcEnabled();
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mReadWriteTagFilters, mTechList);
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        Log.i(TAG, "onNewIntent: " + intent);
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
        Log.i("Mode", ""+mode);

        if (mode.contains("writeManyDataBlocks"))
        {
            resolveWriteManyBlocksIntent(intent, false);
        }
        else if (mode.equals("readUID"))
        {
            resolveReadUIDIntent(intent);
        }
        else if (mode.equals("writeManyValueBlocks"))
        {
            resolveWriteManyBlocksIntent(intent, true);
        }
        else if(mode.equals("readExtraByteAB")) {
            resolveExtraByteAB(intent, true);
        }
        else if(mode.equals("writeExtraByteAB")) {
            resolveExtraByteAB(intent, false);
        }
        else if(mode.equals("readManyValueBlocks")) {
            resolveReadManyBlocks(intent, true);
        }
        else if(mode.equals("readManyDataBlocks")) {
            resolveReadManyBlocks(intent, false);
        }
    }

    /*
     * **** HELPER METHODS ****
     */

    private void checkNfcEnabled()
    {
        Boolean nfcEnabled = mNfcAdapter.isEnabled();
        if (!nfcEnabled)
        {
            new AlertDialog.Builder(MifareControl.this)
                    .setTitle("NFC se encuentra apagado")
                    .setMessage("Encender NFC")
                    .setCancelable(false)
                    .setPositiveButton("Actualizar Settings",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog,
                                                    int id)
                                {
                                    startActivity(new Intent(
                                            android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                                }
                            }).create().show();
        }
    }


    public static byte[] intToValueBlock(int value, int address) {
        ByteBuffer valueBlock = ByteBuffer.allocate(16);

        ByteBuffer temp = ByteBuffer.allocate(12);
        temp.putInt(value);
        temp.putInt(~value);
        temp.putInt(value);

        valueBlock.put(temp.get(3));
        valueBlock.put(temp.get(2));
        valueBlock.put(temp.get(1));
        valueBlock.put(temp.get(0));

        valueBlock.put(temp.get(7));
        valueBlock.put(temp.get(6));
        valueBlock.put(temp.get(5));
        valueBlock.put(temp.get(4));

        valueBlock.put(temp.get(11));
        valueBlock.put(temp.get(10));
        valueBlock.put(temp.get(9));
        valueBlock.put(temp.get(8));

        byte add = (byte)address;
        byte not_add = (byte)~address;
        valueBlock.put(add);
        valueBlock.put(not_add);
        valueBlock.put(add);
        valueBlock.put(not_add);

        return valueBlock.array();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED, new Intent());
        finish();
    }
}
