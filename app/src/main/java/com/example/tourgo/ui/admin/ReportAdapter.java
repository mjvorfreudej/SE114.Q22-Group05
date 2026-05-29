package com.example.tourgo.ui.admin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.ui.admin.AdminMockData.UserReport;

import java.util.ArrayList;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.VH> {

    public interface Listener {
        void onOpen(UserReport report);
    }

    private final List<UserReport> items = new ArrayList<>();
    private final Listener listener;

    public ReportAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<UserReport> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_report, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        UserReport r = items.get(position);
        Context ctx = h.itemView.getContext();

        applySeverity(ctx, h.severity, h.severityDot, h.severityLabel, r.severity);
        h.kind.setText(r.kind);
        h.when.setText(r.when);
        h.body.setText("\"" + r.body + "\"");
        h.reporter.setText(prefixBold(ctx.getString(R.string.adm_by_prefix), r.reporter));
        h.target.setText(prefixBold(ctx.getString(R.string.adm_on_prefix), r.target));

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOpen(r);
        });
    }

    /** Severity → chip bg/fg/dot + label. */
    static void applySeverity(Context ctx, LinearLayout chip, View dot, TextView label, String severity) {
        int bg, fg, dotC;
        String text;
        switch (severity) {
            case "critical":
                bg = R.color.adm_red_100; fg = R.color.adm_red_900; dotC = R.color.adm_red_900; text = "Critical"; break;
            case "mid":
                bg = R.color.adm_amber_100; fg = R.color.adm_amber_700; dotC = R.color.adm_amber_500; text = "Medium"; break;
            case "high":
            default:
                bg = R.color.adm_red_100; fg = R.color.adm_red_700; dotC = R.color.adm_red_500; text = "High"; break;
        }
        chip.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, bg)));
        dot.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, dotC)));
        label.setTextColor(ContextCompat.getColor(ctx, fg));
        label.setText(text);
    }

    private static CharSequence prefixBold(String prefix, String bold) {
        SpannableStringBuilder sb = new SpannableStringBuilder(prefix);
        int start = sb.length();
        sb.append(bold);
        sb.setSpan(new StyleSpan(Typeface.BOLD), start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final LinearLayout severity;
        final View severityDot;
        final TextView severityLabel, kind, when, body, reporter, target;

        VH(@NonNull View v) {
            super(v);
            severity = v.findViewById(R.id.admRepSeverity);
            severityDot = v.findViewById(R.id.admRepSeverityDot);
            severityLabel = v.findViewById(R.id.admRepSeverityLabel);
            kind = v.findViewById(R.id.admRepKind);
            when = v.findViewById(R.id.admRepWhen);
            body = v.findViewById(R.id.admRepBody);
            reporter = v.findViewById(R.id.admRepReporter);
            target = v.findViewById(R.id.admRepTarget);
        }
    }
}
