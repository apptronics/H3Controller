package kr.co.apptronics.acube.example;


import kr.co.apptronics.acube.example.R;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class MyPreferenceActivity extends PreferenceActivity implements OnPreferenceClickListener
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.atp_preference);
        
        Preference pAppName = (Preference)findPreference("setting_activity_id");
        CheckBoxPreference cbpAutoAlarm = (CheckBoxPreference)findPreference("setting_activity_debug_mac");
        EditTextPreference cbpAlarmReceive = (EditTextPreference)findPreference("setting_activity_bt_mac");
        
        
        pAppName.setOnPreferenceClickListener(this);
        
        cbpAutoAlarm.setOnPreferenceClickListener(this);
        cbpAlarmReceive.setOnPreferenceClickListener(this);
        cbpAlarmReceive.setOnPreferenceChangeListener(editTextPreference_OnPreferenceChangeListener);

        
    }
    
    @Override
    public boolean onPreferenceClick(Preference preference)
    {
        // 어플리케이션 이름
        if(preference.getKey().equals("setting_activity_id"))
        {
        }
        // 디버그용 맥 사용
        else if(preference.getKey().equals("setting_activity_debug_mac"))
        {
        }
        // 블루투스 맥 사용
        else if(preference.getKey().equals("setting_activity_bt_mac"))
        {
        }
        return false;
    }
    
    
    private OnPreferenceChangeListener editTextPreference_OnPreferenceChangeListener =  
        new OnPreferenceChangeListener(){  
            @Override  
            public boolean onPreferenceChange(Preference preference, Object newValue) {  
            	if(preference.getKey().equals("setting_activity_debug_mac"))
                {
                }
            	if(preference.getKey().equals("setting_activity_bt_mac"))
                {
            		
                }
            	if(preference.getKey().equals("setting_activity_id"))
                {
            		
                }
            	return true;
        }
   };  
    
} // end of class
