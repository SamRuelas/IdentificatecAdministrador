package android.com.identificatecadministrador;



import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewCredential extends Fragment {

    public NewCredential() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_credential, container, false);
    }

    EditText nameField;
    EditText lastnameField;
    EditText matriculaField;
    EditText academicProgramField;
    EditText passwordField;
    TextView serialNumberField;
    Button readSerialNumber;
    EditText nipField;
    EditText initBalanceField;
    Button uploadDb;
    Button writeCard;
    Converters converter = new Converters();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameField = view.findViewById(R.id.name_edit_text);
        lastnameField = view.findViewById(R.id.lastname_edit_text);
        matriculaField = view.findViewById(R.id.matricula_edit_text);
        academicProgramField = view.findViewById(R.id.academicprogram_edit_text);
        serialNumberField = view.findViewById(R.id.serialnumber_text_view);
        matriculaField = view.findViewById(R.id.matricula_edit_text);
        readSerialNumber = view.findViewById(R.id.read_serialnumber_button);
        nipField = view.findViewById(R.id.nip_edit_text);
        initBalanceField = view.findViewById(R.id.balance_edit_text);
        uploadDb = view.findViewById(R.id.uploaddb_button);
        writeCard = view.findViewById(R.id.writecard_button);
        passwordField = view.findViewById(R.id.password_edit_text);

        readSerialNumber.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), MifareControl.class);
                        intent.putExtra("mode", "readUID");
                        startActivityForResult(intent, 2);
                    }
                }
        );

        writeCard.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!serialNumberField.getText().toString().equals("") &&
                                !nameField.getText().toString().equals("") &&
                                !lastnameField.getText().toString().equals("") &&
                                !matriculaField.getText().toString().equals("") &&
                                !academicProgramField.getText().toString().equals("") &&
                                !nipField.getText().toString().equals("") &&
                                !initBalanceField.getText().toString().equals("") &&
                                !passwordField.getText().toString().equals("")) {

                            User u = new User(
                                    serialNumberField.getText().toString(),
                                    nameField.getText().toString(),
                                    matriculaField.getText().toString().toUpperCase(),
                                    academicProgramField.getText().toString(),
                                    Integer.parseInt(nipField.getText().toString()),
                                    Integer.parseInt(initBalanceField.getText().toString()),
                                    new ArrayList<Integer>(),
                                    new ArrayList<Integer>(),
                                    false,
                                    ""
                            );
                            Intent intent = new Intent(v.getContext(), MifareControl.class);

                            // Saldo y dirección de última transacción
                            intent.putExtra("mode", "writeManyValueBlocks");
                            ArrayList<Integer> writeValueBlocks = new ArrayList<>();
                            writeValueBlocks.add(u.getInitialBalance());
                            writeValueBlocks.add(4);
                            intent.putExtra("writeValueBlocks", writeValueBlocks);

                            int initialBlock = 2;
                            intent.putExtra("initialBlock", initialBlock);
                            startActivityForResult(intent, 3);

                            // Matricula
                            intent.putExtra("mode", "writeManyDataBlocks");
                            ArrayList<String> writeDataBlocks = new ArrayList<>();
                            writeDataBlocks.add("00000000000000"+converter.asciiHexFromString(u.getMatricula()));
                            intent.putExtra("writeDataBlocks", writeDataBlocks);

                            initialBlock = 1;
                            intent.putExtra("initialBlock", initialBlock);
                            startActivityForResult(intent, 3);

                            // Perdida/encontrada
                            intent.putExtra("mode", "writeExtraByteAB");
                            intent.putExtra("extraByteABSector", 3);
                            intent.putExtra("extraByteABSectorValue", "77");
                            startActivityForResult(intent, 4);
                        }
                        else {
                            Toast.makeText(getContext(), "Llena todos los campos", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        uploadDb.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!serialNumberField.getText().toString().equals("") &&
                                !nameField.getText().toString().equals("") &&
                                !lastnameField.getText().toString().equals("") &&
                                !matriculaField.getText().toString().equals("") &&
                                !academicProgramField.getText().toString().equals("") &&
                                !nipField.getText().toString().equals("") &&
                                !initBalanceField.getText().toString().equals("") &&
                                !passwordField.getText().toString().equals("")) {

                            User u = new User(
                                    serialNumberField.getText().toString(),
                                    nameField.getText().toString(),
                                    matriculaField.getText().toString(),
                                    academicProgramField.getText().toString(),
                                    Integer.parseInt(nipField.getText().toString()),
                                    Integer.parseInt(initBalanceField.getText().toString()),
                                    new ArrayList<Integer>(),
                                    new ArrayList<Integer>(),
                                    false,
                                    ""
                            );
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(u.getMatricula()+"@itesm.mx", passwordField.getText().toString());

                            DatabaseReference database = FirebaseDatabase.getInstance().getReference().child(u.getMatricula());
                            database.child("serialNumber").setValue(u.getSerialNumber());
                            database.child("name").setValue(u.getName());
                            database.child("academicProgram").setValue(u.getAcademicProgram());
                            database.child("nip").setValue(u.getNip());
                            database.child("balance").setValue(u.getInitialBalance());
                            database.child("deposits").child(new SimpleDateFormat("dd:MM:yyyy HH:mm:ss").format(new Date())).setValue(u.getInitialBalance());
                            database.child("charges");
                            database.child("isLost").setValue(false);
                            database.child("foundBy").setValue(false);

                            Toast.makeText(getContext(), "Subido a base de datos", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getContext(), "Llena todos los campos", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("Received", "RC: " + requestCode + ", RESc" + resultCode);
        if (resultCode == RESULT_OK && requestCode == 2) {
            serialNumberField.setText(data.getStringExtra("UID"));
        }
        if (resultCode == RESULT_OK && requestCode == 4) {
            Toast.makeText(getContext(), "Tarjeta actualizada", Toast.LENGTH_SHORT).show();
        }
    }

}
