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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.app.Activity.RESULT_OK;


public class DepositCredential extends Fragment {

    EditText matricula;
    EditText deposito;
    Button botonbd;;
    int balance;
    int depositoText;
    String matriculaText;

    public DepositCredential() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_deposit, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        matricula = view.findViewById(R.id.MatriculaDeposito);
        deposito = view.findViewById(R.id.DepositoD);
        botonbd = view.findViewById(R.id.BaseB);

        botonbd.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {

                        if (!matricula.getText().toString().equals("") && !deposito.getText().toString().equals("")) {
                            matriculaText = matricula.getText().toString().toUpperCase();
                            depositoText = Integer.parseInt(deposito.getText().toString());

                            final DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                            db.addListenerForSingleValueEvent( new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    try {
                                        DataSnapshot ref = dataSnapshot.child(matriculaText);
                                        balance = Integer.parseInt(ref.child("balance").getValue().toString());

                                        db.child(matriculaText).child("balance").setValue(balance+depositoText);
                                        db.child(matriculaText).child("deposits").child(new SimpleDateFormat("dd:MM:yyyy HH:mm:ss").format(new Date()))
                                                .setValue(depositoText);

                                        // Leer balance
                                        Intent intent = new Intent(v.getContext(), MifareControl.class);
                                        intent.putExtra("mode", "readManyValueBlocks");
                                        intent.putExtra("readInitialBlock", 2);
                                        intent.putExtra("blocksToRead", 1);
                                        startActivityForResult(intent, 1);
                                    }
                                    catch (Exception e) {
                                        Toast.makeText(getContext(), "No existe matrícula", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                }

                            });
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
        Log.i("Received", "RC: "+requestCode+", RESc"+resultCode);
        if(resultCode == RESULT_OK && requestCode == 1) {
            if (data.getBooleanExtra("readManyBlocksNoErrors", false)) {
                int balanceTarjeta = data.getIntegerArrayListExtra("blocksRead").get(0);
                Log.i("balance", ""+balanceTarjeta);

                // Saldo actualizado
                Intent intent = new Intent(getContext(), MifareControl.class);
                intent.putExtra("mode", "writeManyValueBlocks");
                ArrayList<Integer> writeValueBlocks = new ArrayList<>();
                writeValueBlocks.add(balanceTarjeta+depositoText);
                intent.putExtra("writeValueBlocks", writeValueBlocks);
                intent.putExtra("initialBlock", 2);
                startActivityForResult(intent, 2);
            }
        }
        else if(resultCode == RESULT_OK && requestCode == 2) {
            Toast.makeText(getContext(), "Depósito realizado",Toast.LENGTH_LONG).show();
        }
    }
}

