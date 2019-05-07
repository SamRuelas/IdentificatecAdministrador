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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class ReadCredential extends Fragment {

    TextView matricula;
    TextView balance;
    TextView isLost;
    Button readCredential;
    Converters converter = new Converters();

    public ReadCredential() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_read_credential, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matricula = view.findViewById(R.id.matricula_read_edit_text);
        balance = view.findViewById(R.id.balance_read_edit_text);
        isLost = view.findViewById(R.id.islost_read_edit_text);
        readCredential = view.findViewById(R.id.read_credential_button);

        readCredential.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), MifareControl.class);

                        // Leer matricula
                        intent.putExtra("mode", "readManyDataBlocks");
                        intent.putExtra("readInitialBlock", 1);
                        intent.putExtra("blocksToRead", 1);
                        startActivityForResult(intent, 1);

                        // Leer balance, dirección de último cargo y recientes
                        intent.putExtra("mode", "readManyValueBlocks");
                        intent.putExtra("readInitialBlock", 2);
                        intent.putExtra("blocksToRead", 46);
                        startActivityForResult(intent, 2);

                        // Leer isLost
                        intent.putExtra("mode", "readExtraByteAB");
                        intent.putExtra("extraByteABSector", 3);
                        startActivityForResult(intent, 3);
                    }
                }
        );

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("Received", "RC: "+requestCode+", RESc"+resultCode);
        if(resultCode == RESULT_OK && requestCode == 1) {
            if (data.getBooleanExtra("readManyBlocksNoErrors", false)) {
                matricula.setText(converter.stringFromHexAscii(data.getStringArrayListExtra("blocksRead").get(0).substring(14)));
            }
        }
        else if(resultCode == RESULT_OK && requestCode == 2) {
            if (data.getBooleanExtra("readManyBlocksNoErrors", false)) {
                balance.setText(data.getIntegerArrayListExtra("blocksRead").get(0).toString());

                List fullArray = data.getIntegerArrayListExtra("blocksRead");
                List<Integer> chargesList = fullArray.subList(2,fullArray.size()-1);
                ArrayList<String> chargesListPrint = new ArrayList<>();
                for(Integer charge : chargesList){
                    if(charge!=0){
                        chargesListPrint.add('$'+charge.toString());
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(),
                        R.layout.deposit_item, chargesListPrint);
                ListView listView = getActivity().findViewById(R.id.listview_recent_charges);
                listView.setAdapter(adapter);

            }
        }
        else if(resultCode == RESULT_OK && requestCode == 3) {
            if (data.getBooleanExtra("readExtraByteABNoErrors", false)) {
                isLost.setText(data.getStringExtra("extraByteAB").equals("77") ? "No perdida" : "Perdida");
                Toast.makeText(getContext(), "Lectura exitosa", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

