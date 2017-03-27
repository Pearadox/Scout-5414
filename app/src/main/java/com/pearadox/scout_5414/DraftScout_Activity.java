package com.pearadox.scout_5414;

import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.cpjd.main.Settings;
import com.cpjd.main.TBA;
import com.cpjd.models.Event;
import com.cpjd.models.Team;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class DraftScout_Activity extends AppCompatActivity {

    String TAG = "DraftScout_Activity";        // This CLASS name
    TextView txt_EventName, txt_NumTeams;
    ListView lstView_Teams;
    TextView TeamData, BA, Stats;
    ArrayAdapter<String> adaptTeams;
//    ArrayList<String> draftList = new ArrayList<String>();
    static final ArrayList<HashMap<String,String>> draftList =
            new ArrayList<HashMap<String,String>>();
    public int teamSelected = 0;
    String tnum = "";
    Team[] teams;
    public static int BAnumTeams = 0;                                      // # of teams from Blue Alliance
    String gearChk=""; String climbChk=""; String climbRatio=""; String autoRatio=""; String teleRatio="";
    String load_team;
    private FirebaseDatabase pfDatabase;
    private DatabaseReference pfMatchData_DBReference;
    matchData match_inst = new matchData();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft_scout);

        Log.i(TAG, "@@@@@ DraftScout_Activity  @@@@@");
        txt_EventName = (TextView) findViewById(R.id.txt_EventName);
        txt_NumTeams = (TextView) findViewById(R.id.txt_NumTeams);
        lstView_Teams = (ListView) findViewById(R.id.lstView_Teams);
        txt_EventName.setText(Pearadox.FRC_EventName);              // Event Name
        txt_NumTeams.setText(String.valueOf(Pearadox.numTeams));    // # of Teams
        Log.w(TAG, "***** Matches Loaded. # = "  + Pearadox.Matches_Data.size());

        pfDatabase = FirebaseDatabase.getInstance();
        pfMatchData_DBReference = pfDatabase.getReference("match-data/" + Pearadox.FRC_Event);    // Match Data

        SimpleAdapter adaptTeams = new SimpleAdapter(
                this,
                draftList,
                R.layout.draft_list_layout,
                new String[] {"team","BA","Stats"},
                new int[] {R.id.TeamData,R.id.BA, R.id.Stats}
        );

        loadTeams();

        lstView_Teams.setAdapter(adaptTeams);
        adaptTeams.notifyDataSetChanged();


// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        lstView_Teams.setOnItemClickListener(new AdapterView.OnItemClickListener()	{
            public void onItemClick(AdapterView<?> parent,
                                    View view, int pos, long id) {
                Log.w(TAG,"*** lstView_Teams ***   Item Selected: " + pos);
                teamSelected = pos;
                lstView_Teams.setSelector(android.R.color.holo_blue_light);
        		/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
//                tnum = draftList.get(teamSelected).substring(0,4);
            }
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing.
            }
        });
    }


// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    public void buttonUp_Click(View view) {
        Log.i(TAG, " buttonUp_Click   " );

    }

    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    public void buttonDown_Click(View view) {
        Log.i(TAG, " buttonDown_Click   " );

    }

    private void loadTeams() {
        Log.i(TAG, "@@@@  loadTeams started  @@@@");
// ----------  Blue Alliance  -----------
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);
        TBA.setID("Pearadox", "Scout-5414", "V1");
        final TBA tba = new TBA();
        Settings.FIND_TEAM_RANKINGS = true;
        Settings.GET_EVENT_TEAMS = true;
        Settings.GET_EVENT_MATCHES = true;
        Settings.GET_EVENT_ALLIANCES = true;
        Settings.GET_EVENT_AWARDS = true;
        Settings.GET_EVENT_STATS = true;
        String tn = "";

        TBA t = new TBA();
        Event e = t.getEvent(Pearadox.FRC_Event, 2017);
        teams = e.teams.clone();
//        Team[] teams1 = e.teams;
        Log.e(TAG, Pearadox.FRC_Event + "Teams= " + teams.length);
        draftList.clear();
        BAnumTeams = e.teams.length;
        if (BAnumTeams > 0) {
            for (int i = 0; i < teams.length; i++) {
                HashMap<String, String> temp = new HashMap<String, String>();
                if (String.valueOf(teams[i].team_number).length() < 4) {
                    tn = " " + String.valueOf(teams[i].team_number);    // Add leading blank
                } else {
                    tn = String.valueOf(teams[i].team_number);
                }

                teamData(tn);   // Get Team's Match Data

                temp.put("team", tn + " - " + teams[i].nickname);
                temp.put("BA", "Rank=" + teams[i].rank + "  " + teams[i].record + "   OPR=" + String.format("%3.1f", (teams[i].opr)) + "    ↑ " + String.format("%3.1f", (teams[i].touchpad)) + "   kPa=" + String.format("%3.1f", (teams[i].pressure)));
                temp.put("Stats", "Auto Gears=" + autoRatio + "  Tele Gears=" + teleRatio + "   Pick up Gears " + gearChk + "   Climb " + climbChk + "  " + climbRatio);
                draftList.add(temp);
                                                      } // End For
                Log.w(TAG, "### Teams ###  : " + draftList.size());

        } else {
            final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            tg.startTone(ToneGenerator.TONE_PROP_BEEP);
            Toast toast = Toast.makeText(getBaseContext(), "***  Data from the Blue Alliance is _NOT_ available this session  ***", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }

    }

    private void teamData(String team) {
        Log.i(TAG, "$$$$  teamData  $$$$ " + team);
        int autoGears = 0; int teleGears = 0;int teleAttempt = 0; int climbs = 0; int climbAttemps = 0; int numMatches = 0;
        boolean gear_pu =false;

        for (int i = 0; i < Pearadox.Matches_Data.size(); i++) {
            match_inst = Pearadox.Matches_Data.get(i);      // Get instance of Match Data
            if (match_inst.getTeam_num().matches(team)) {
                Log.e(TAG, i + "  " + match_inst.getMatch() + "  Team=" + team);
                numMatches++;
                autoGears = autoGears + match_inst.getAuto_gears_placed();
                Log.w(TAG, "Auto Gears = " + match_inst.getAuto_gears_placed());
                teleGears = teleGears + match_inst.getTele_gears_placed();
                Log.w(TAG, "Tele Gears Placed = " + match_inst.getTele_gears_placed());
                teleAttempt = teleAttempt + match_inst.getTele_gears_attempt();
                Log.w(TAG, "Tele Gears Attempted = " + match_inst.getTele_gears_attempt());
                if (match_inst.isTele_climb_attempt()) {
                    climbAttemps++;
                    Log.w(TAG, "Tele Climb Attempt Number= " + climbAttemps);
                }
                if (match_inst.isTele_climb_success()) {
                    climbs++;
                    Log.w(TAG, "Tele Climb Success Number= " + climbs);
                }
                if (match_inst.isTele_gear_pickup()) {
                    gear_pu = true;
                    Log.w(TAG, "Tele Climb Attempt Number= ");
                }

            }
        } // End For

        if (numMatches > 0) {
            if (gear_pu) {
                gearChk = "❎";
            } else {
                gearChk = "⎕";
            }
            if (teleAttempt > 0) {
                climbChk = "❎";
            } else {
                climbChk = "⎕";
            }
            autoRatio = autoGears + "/" + numMatches;
            teleRatio = teleGears + "/" + teleAttempt;
            climbRatio = climbs + "/" + climbAttemps;
        } else {
            gearChk = "⎕";
            climbChk = "⎕";
            autoRatio = "0/0";
            teleRatio = "0/0";
            climbRatio = "0/0";
        }
    }

    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    private void addMD_VE_Listener(final DatabaseReference pfMatchData_DBReference) {
        pfMatchData_DBReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "<<<< getFB_Data >>>> Match Data for team " + load_team);
                Pearadox.Matches_Data.clear();
                matchData mdobj = new matchData();
                Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();   /*get the data children*/
                Iterator<DataSnapshot> iterator = snapshotIterator.iterator();
                while (iterator.hasNext()) {
                    mdobj = iterator.next().getValue(matchData.class);
                    Pearadox.Matches_Data.add(mdobj);
                }
                Log.w(TAG, "***** Matches Loaded. # = "  + Pearadox.Matches_Data.size());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                /*listener failed or was removed for security reasons*/
            }
        });
    }



//###################################################################
//###################################################################
//###################################################################
@Override
public void onStart() {
        super.onStart();
        Log.v(TAG, "onStart");

        addMD_VE_Listener(pfMatchData_DBReference);        // Load Matches

}
@Override
public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        }
@Override
public void onStop() {
        super.onStop();
        Log.v(TAG, "onStop");
        }

@Override
public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "OnDestroy");
        }

}
