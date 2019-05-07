package android.com.identificatecadministrador;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.app.Activity.RESULT_OK;


/*
 * A simple {@link Fragment} subclass.
 */
public class ChargeCredential extends Fragment {

    TextView matricula;
    TextView balance;
    EditText chargeAmount;
    EditText nipField;
    Button charge;
    Button readCredential;
    int lastChargeAddress;
    Converters converter = new Converters();

    public ChargeCredential() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_charge_credential, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        matricula = view.findViewById(R.id.matricula_text_view_charge);
        balance = view.findViewById(R.id.balance_text_view_charge);
        chargeAmount = view.findViewById(R.id.charge_edit_text_charge);
        charge = view.findViewById(R.id.charge_button);
        nipField = view.findViewById(R.id.nip_edit_text_charge);
        readCredential = view.findViewById(R.id.read_credential_charge_button);

        charge.setEnabled(false);

        final Intent intent = new Intent(getContext(), MifareControl.class);

        readCredential.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Leer matricula
                        intent.putExtra("mode", "readManyDataBlocks");
                        intent.putExtra("readInitialBlock", 1);
                        intent.putExtra("blocksToRead", 1);
                        startActivityForResult(intent, 0);
                    }
                }
        );

        charge.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String chargeAmountStr = chargeAmount.getText().toString();
                        if (!chargeAmountStr.equals("") && !balance.getText().toString().equals("") && !nipField.getText().toString().equals("")) {
                            final int newBalance = Integer.parseInt(balance.getText().toString())-Integer.parseInt(chargeAmountStr);
                            if (newBalance >= 0) {
                                try {
                                    final DatabaseReference database = FirebaseDatabase.getInstance().getReference().child(matricula.getText().toString());
                                    database.addListenerForSingleValueEvent(
                                            new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    Boolean isLost = (Boolean) dataSnapshot.child("isLost").getValue();
                                                    if (!isLost) {
                                                        String nip = dataSnapshot.child("nip").getValue().toString();
                                                        if (nip.equals(nipField.getText().toString())) {
                                                            database.child("balance").setValue(newBalance);
                                                            database.child("charges").child(new SimpleDateFormat("dd:MM:yyyy HH:mm:ss").format(new Date())).setValue(Integer.parseInt(chargeAmount.getText().toString()));

                                                            // Saldo
                                                            intent.putExtra("mode", "writeManyValueBlocks");
                                                            ArrayList<Integer> writeValueBlocks = new ArrayList<>();
                                                            writeValueBlocks.add(newBalance);

                                                            int finalAddress = (((lastChargeAddress+2)%4) == 0) ? lastChargeAddress+2 : lastChargeAddress+1;

                                                            if(finalAddress==62){
                                                                finalAddress=5;
                                                            }

                                                            writeValueBlocks.add(finalAddress);
                                                            intent.putExtra("writeValueBlocks", writeValueBlocks);
                                                            intent.putExtra("initialBlock", 2);
                                                            startActivityForResult(intent, 3);

                                                            // Último cargo
                                                            intent.putExtra("mode", "writeManyValueBlocks");
                                                            writeValueBlocks.clear();
                                                            writeValueBlocks.add(Integer.parseInt(chargeAmount.getText().toString()));
                                                            intent.putExtra("writeValueBlocks", writeValueBlocks);
                                                            intent.putExtra("initialBlock", finalAddress);
                                                            startActivityForResult(intent, 4);
                                                        }
                                                        else {
                                                            Toast.makeText(getContext(), "NIP erróneo", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                    else {
                                                        Toast.makeText(getContext(), "Credencial perdida.Cambiando estado.", Toast.LENGTH_SHORT).show();
                                                        intent.putExtra("mode", "writeExtraByteAB");
                                                        intent.putExtra("extraByteABSectorValue", "00");
                                                        intent.putExtra("extraByteABSector", 3);
                                                        startActivityForResult(intent, 5);
                                                    }

                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            }
                                    );
                                }
                                catch(Exception e) {
                                    Toast.makeText(v.getContext(), "No hay registro de matrícula", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(v.getContext(), "Saldo insuficiente", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(v.getContext(), "Introduce un cargo y NIP, y/o lee la credencial", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("Received", "RC: "+requestCode+", RESc"+resultCode);
        final Intent intent = new Intent(getContext(), MifareControl.class);

        if (resultCode == RESULT_OK && requestCode == 0) {
            if (data.getBooleanExtra("readManyBlocksNoErrors", false)) {
                matricula.setText(converter.stringFromHexAscii(data.getStringArrayListExtra("blocksRead").get(0).substring(14)));

                intent.putExtra("mode", "readExtraByteAB");
                intent.putExtra("extraByteABSector", 3);
                startActivityForResult(intent, 1);
            }
        }
        else if(resultCode == RESULT_OK && requestCode == 1) {
            if (data.getBooleanExtra("readExtraByteABNoErrors", false)) {
                if (data.getStringExtra("extraByteAB").equals("77")) {

                    // Leer balance y dirección de último cargo
                    intent.putExtra("mode", "readManyValueBlocks");
                    intent.putExtra("readInitialBlock", 2);
                    intent.putExtra("blocksToRead", 2);
                    startActivityForResult(intent, 2);

                    charge.setEnabled(true);
                }
                else {
                    Toast.makeText(getContext(), "Credencial registrada como perdida", Toast.LENGTH_SHORT).show();
                    charge.setEnabled(false);
                }

            }
        }
        else if(resultCode == RESULT_OK && requestCode == 2) {
            if (data.getBooleanExtra("readManyBlocksNoErrors", false)) {

                balance.setText(data.getIntegerArrayListExtra("blocksRead").get(0).toString());
                lastChargeAddress = data.getIntegerArrayListExtra("blocksRead").get(1);
            }
        }
        else if(resultCode == RESULT_OK && requestCode == 4) {
            if (data.getBooleanExtra("writeManyBlocksNoErrors", false)) {
                Toast.makeText(getActivity().getApplicationContext(), "Cargo realizado", Toast.LENGTH_SHORT).show();

            }
        }
    }
}
