package com.parse.starter;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ParseRelation;
import com.parse.GetCallback;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by yiningchen on 7/26/15.
 */
public class GroupDetail_Fragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        final String StrGroupID = bundle.getString("GroupID", "DEFAULT");

        //inflate and return the layout
        final View v = inflater.inflate(R.layout.fragment_groupdetail, container, false);
        final Button button2;
        button2 = (Button) v.findViewById(R.id.button2);
        button2.setClickable(true);

        // retrieve from parse
        ParseQuery<ParseObject> query = ParseQuery.getQuery("StudyGroups");
        query.getInBackground(StrGroupID, new GetCallback<ParseObject>() {
            public void done(ParseObject TargetGroup, ParseException e) {
                if (e == null) {
                    // TargetGroup will be the result

                    TextView dept, course, spinnerDate, spinnerHour, spinnerMaxNum;
                    TextView LocEditTxt, editText, nameEditTxt, CurNum;


                    dept = (TextView) v.findViewById(R.id.spinner1);
                    course = (TextView) v.findViewById(R.id.spinner2);
                    spinnerDate = (TextView) v.findViewById(R.id.date);
                    spinnerHour = (TextView) v.findViewById(R.id.hour);
                    spinnerMaxNum = (TextView) v.findViewById(R.id.spinnerMaxNum);
                    LocEditTxt = (TextView) v.findViewById(R.id.LocEditTxt);
                    editText = (TextView) v.findViewById(R.id.editText);
                    nameEditTxt = (TextView) v.findViewById(R.id.nameEditTxt);
                    CurNum = (TextView) v.findViewById(R.id.CurNum);

                    dept.setText(TargetGroup.getString("Department"));
                    course.setText(TargetGroup.getString("Class"));
                    spinnerDate.setText(TargetGroup.getString("Month") + "/" + TargetGroup.getString("Day") + "/" + TargetGroup.getString("Year"));
                    spinnerHour.setText(TargetGroup.getString("Hour") + ":" + TargetGroup.getString("Minute") + " " + TargetGroup.getString("AMPM"));
                    int u = TargetGroup.getInt("CurNum");
                    CurNum.setText(Integer.toString(u));
                    u = TargetGroup.getInt("MaxNum");
                    spinnerMaxNum.setText(Integer.toString(u));
                    LocEditTxt.setText(TargetGroup.getString("Loc"));
                    editText.setText(TargetGroup.getString("Info"));
                    nameEditTxt.setText(TargetGroup.getString("GroupName"));

                    // conditions for able to join group

                    // 1. CurNum < MaxNum
                    if (TargetGroup.getInt("CurNum")==TargetGroup.getInt("MaxNum")) {
                        button2.setClickable(false);
                        button2.setText("Group Full");
                    }

                    // 2. Current User not already in group
                    ParseRelation relation = TargetGroup.getRelation("MyMembers");
                    ParseQuery query2 = relation.getQuery();

                    query2.findInBackground(new FindCallback<ParseObject>() {
                        public void done(List<ParseObject> userList, ParseException e) {
                            if (e == null) {
                                ParseUser user = ParseUser.getCurrentUser();
                                if (userList.contains(user)) {
                                    button2.setClickable(false);
                                    button2.setText("Group joined");
                                }

                                TextView curMem;
                                curMem = (TextView) v.findViewById(R.id.curMem);
                                String MemList="";
                                boolean isFirst=true;
                                for (ParseObject curMemX : userList) {
                                    if (!isFirst)  MemList = MemList+"\n";
                                    isFirst = false;
                                    MemList = MemList + ((ParseUser) curMemX).getUsername();
                                }
                                curMem.setText(MemList);
                            } else {
                                Log.d("query2", "Error: " + e.getMessage());
                            }
                        }
                    });

                } else {
                    // something went wrong
                    Log.d("group details page ", "Error: " + e.getMessage());
                }
            }

        });

        // Join Group button
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Toast.makeText(getActivity(),
                //        "Group joined",
                //        Toast.LENGTH_SHORT).show();

                // Create user - group relation
                ParseQuery<ParseObject> query = ParseQuery.getQuery("StudyGroups");
                query.getInBackground(StrGroupID, new GetCallback<ParseObject>() {
                    public void done(final ParseObject TargetGroup, ParseException e) {
                        if (e == null) {
                            // add TargetGroup to user
                            final ParseUser user = ParseUser.getCurrentUser();
                            ParseRelation relation = user.getRelation("MyGroups");
                            relation.add(TargetGroup);
                            user.saveInBackground();

                            ParseRelation relation2 = TargetGroup.getRelation("MyMembers");
                            relation2.add(user);
                            TargetGroup.setObjectId(StrGroupID);
                            TargetGroup.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        // increment CurNum
                                        TargetGroup.increment("CurNum");
                                        TargetGroup.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                //dummy
                                                if (e == null)
                                                    System.out.println("group joined!!!");
                                            }
                                        });
                                    } else {
                                        Log.d("weird!!!", "Error: " + e.getMessage());
                                    }
                                }
                            });

                        } else {
                            // something went wrong
                            Log.d("group details page ", "Error: " + e.getMessage());
                        }
                    }

                });
                button2.setText("Group joined");
                button2.setClickable(false);
            }
        });

        return v;
    }
}
