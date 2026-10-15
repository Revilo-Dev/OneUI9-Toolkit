package dev.oneuiproject.oneuiexample.fragment;

import dev.oneuiproject.oneui.widget.OneUIPalette;

import android.os.Bundle;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.sec.sesl.tester.R;

import java.util.ArrayList;
import java.util.List;

import dev.oneuiproject.oneuiexample.activity.MainActivity;
import dev.oneuiproject.oneuiexample.base.BaseFragment;
import dev.oneuiproject.oneui.widget.FloatingSearchBar;
import dev.oneuiproject.oneui.widget.ScrollEdgeFades;

public class WidgetsFragment extends BaseFragment
        implements View.OnClickListener {
    private FloatingSearchBar floatingSearch;
    private final ActivityResultLauncher<Intent> voiceSearch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (floatingSearch == null || result.getData() == null) return;
                ArrayList<String> matches = result.getData()
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (matches != null && !matches.isEmpty()) {
                    if (!floatingSearch.isOpen()) floatingSearch.open(null);
                    floatingSearch.setQuery(matches.get(0), true);
                }
            });

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int[] Ids = {R.id.fragment_btn_1,
                R.id.fragment_btn_2,
                R.id.fragment_btn_3,
                R.id.fragment_btn_4,
                R.id.fragment_btn_5};
        for (int id : Ids) view.findViewById(id).setOnClickListener(this);

        AppCompatSpinner spinner = view.findViewById(R.id.fragment_spinner);
        List<String> items = new ArrayList<>();
        for (int i = 1; i < 5; i++)
            items.add("Spinner Item " + i);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        FloatingSearchBar search = view.findViewById(R.id.widgets_floating_search);
        floatingSearch = search;
        AppCompatButton showSearch = view.findViewById(R.id.widgets_show_floating_search);
        SwitchCompat persistent = view.findViewById(R.id.widgets_persistent_search);
        SwitchCompat ai = view.findViewById(R.id.widgets_ai_search);
        View content = view.findViewById(R.id.widgets_scroll_content);
        ScrollView results = view.findViewById(R.id.widgets_search_results);
        LinearLayout rows = view.findViewById(R.id.widgets_search_result_rows);
        search.setSourceView(content);
        search.setHint("Search pages");
        search.setPersistent(persistent.isChecked());
        search.setAiMode(ai.isChecked());
        showSearch.setOnClickListener(v -> {
            if (search.isOpen()) search.close();
            else search.open(null);
        });
        persistent.setOnCheckedChangeListener((button, checked) -> {
            search.setPersistent(checked);
            showSearch.setText(search.isOpen() ? "Dismiss floating search" : "Show floating search");
        });
        ai.setOnCheckedChangeListener((button, checked) -> search.setAiMode(checked));
        search.setOnVoiceRequested(() -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                    .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            if (intent.resolveActivity(requireContext().getPackageManager()) != null)
                voiceSearch.launch(intent);
        });
        search.setListener(new FloatingSearchBar.Listener() {
            @Override public void onQueryChanged(String query) {
                boolean hasQuery = !query.trim().isEmpty();
                results.setVisibility(hasQuery ? View.VISIBLE : View.GONE);
                if (hasQuery)
                    ((MainActivity) requireActivity()).populatePageSearchResults(rows, query);
                search.setSourceView(hasQuery ? results : content);
            }
            @Override public void onVisibilityChanged(boolean visible) {
                showSearch.setText(visible ? "Dismiss floating search" : "Show floating search");
            }
        });
        ScrollEdgeFades.attach(results).setColor(OneUIPalette.color(requireContext(), R.color.oui_background_color));
    }

    @Override public void onDestroyView() {
        floatingSearch = null;
        super.onDestroyView();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.sample3_fragment_widgets;
    }

    @Override
    public int getIconResId() {
        return R.drawable.ic_oui_game_launcher;
    }

    @Override
    public CharSequence getTitle() {
        return "Widgets";
    }

    @Override
    public void onClick(View v) {
        // no-op
    }

}
