package com.example.practicalfive;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Municipality extends Activity {
    public static final String EXTRA_MUNICIPALITY = "com.example.practicalfive.MUNICIPALITY";
    private String selectedMunicipality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedMunicipality = new TextProcessor().processMunicipality(
                getIntent().getStringExtra(EXTRA_MUNICIPALITY));
        if (selectedMunicipality == null) {
            Toast.makeText(this, R.string.invalid_details_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_municipality);
        UiInsets.apply(this, R.id.municipality_root);
        displayMunicipality(selectedMunicipality);
        Button visitSite = findViewById(R.id.visit_site);
        visitSite.setOnClickListener(view -> openOfficialWebsite());
    }

    private void displayMunicipality(String name) {
        int summaryIndex;
        int imageResource;
        int colourResource;
        // Keep these indexes aligned with the written assignment order in arrays.xml.
        switch (name) {
            case "Beijing":
                summaryIndex = 0;
                imageResource = R.drawable.beijing;
                colourResource = R.color.municipality_beijing;
                break;
            case "Shanghai":
                summaryIndex = 1;
                imageResource = R.drawable.shanghai;
                colourResource = R.color.municipality_shanghai;
                break;
            case "Chongqing":
                summaryIndex = 2;
                imageResource = R.drawable.chongqing;
                colourResource = R.color.municipality_chongqing;
                break;
            case "Tianjin":
                summaryIndex = 3;
                imageResource = R.drawable.tianjin;
                colourResource = R.color.municipality_tianjin;
                break;
            default:
                // A second guard keeps future changes to the supported-name list safe.
                finish();
                return;
        }
        int colour = getColor(colourResource);
        boolean brightBackground = "Chongqing".equals(name);
        int foreground = getColor(brightBackground
                ? R.color.color_text_dark : R.color.color_text_light);
        TextView municipalityName = findViewById(R.id.municipality_name);
        municipalityName.setText(name);
        municipalityName.setBackgroundColor(colour);
        municipalityName.setTextColor(foreground);
        ImageView image = findViewById(R.id.municipality_image);
        image.setImageResource(imageResource);
        image.setContentDescription(getString(R.string.city_image_description, name));
        TextView information = findViewById(R.id.municipality_information);
        information.setText(getResources().getStringArray(R.array.municipalities_array)[summaryIndex]);
        information.setTextColor(colour);
        ScrollView informationScroll = findViewById(R.id.information_scroll);
        // Bright orange remains the required text colour; a dark panel preserves contrast.
        informationScroll.setBackgroundColor(getColor(brightBackground
                ? R.color.color_dark_information_background : R.color.color_surface));
        Button visitSite = findViewById(R.id.visit_site);
        visitSite.setBackgroundTintList(ColorStateList.valueOf(colour));
        visitSite.setTextColor(foreground);
    }

    private void openOfficialWebsite() {
        String url;
        switch (selectedMunicipality) {
            case "Beijing":
                url = "https://english.beijing.gov.cn/";
                break;
            case "Shanghai":
                url = "https://english.shanghai.gov.cn/";
                break;
            case "Chongqing":
                url = "https://english.cq.gov.cn/";
                break;
            case "Tianjin":
                url = "https://en.tj.gov.cn/";
                break;
            default:
                return;
        }
        // Leave the component unset so Android can choose a suitable browser.
        Intent website = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            startActivity(website);
        } catch (ActivityNotFoundException unavailable) {
            Toast.makeText(this, R.string.no_browser_error, Toast.LENGTH_LONG).show();
        } catch (SecurityException blocked) {
            Toast.makeText(this, R.string.website_unavailable_error, Toast.LENGTH_LONG).show();
        }
    }
}
