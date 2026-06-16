package com.example.tourgo.ui.admin;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.tourgo.R;
import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.data.repository.FavoriteRepository;
import com.example.tourgo.data.repository.HotelRepository;
import com.example.tourgo.data.repository.TourRepository;
import com.example.tourgo.data.repository.UserRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.AdminAuditEntry;
import com.example.tourgo.models.response.AdminTeamMember;
import com.example.tourgo.remote.service.AdminService;
import com.example.tourgo.ui.auth.LoginActivity;
import com.example.tourgo.ui.notification.NotificationMockData;
import com.example.tourgo.ui.notification.NotificationsActivity;
import com.example.tourgo.utils.LocaleHelper;

import java.util.List;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

/** Admin › Profile / Settings — account card, moderation & preferences rows, log out. */
public class AdminProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        row(v, R.id.rowModPolicy, R.drawable.ic_gavel, R.string.adm_mod_policy, 0, this::openModerationPolicy);
        row(v, R.id.rowAdminTeam, R.drawable.ic_users, R.string.adm_admin_team, R.string.adm_admin_team_count, this::openAdminTeam);
        row(v, R.id.rowAuditLog, R.drawable.ic_history, R.string.adm_audit_log, 0, this::openAuditLog);
        row(v, R.id.rowNotifications, R.drawable.ic_bell_20, R.string.adm_notifications, 0, this::openNotifications);
        row(v, R.id.rowLanguage, R.drawable.ic_globe_24, R.string.adm_language, 0, this::openLanguage);
        row(v, R.id.rowPassword, R.drawable.ic_lock_24, R.string.adm_password_2fa, 0, this::openSecurity);
        row(v, R.id.rowHelp, R.drawable.ic_help_circle, R.string.adm_help_center, 0, this::openHelp);

        // The language row trails the currently active language (native name).
        setLanguageTrailing(v);

        v.findViewById(R.id.admBtnLogout).setOnClickListener(view -> showLogoutDialog());
    }

    private void setLanguageTrailing(View root) {
        View row = root.findViewById(R.id.rowLanguage);
        TextView trailing = row.findViewById(R.id.admRowTrailing);
        trailing.setText("vi".equals(LocaleHelper.getCurrentLanguageTag()) ? "Tiếng Việt" : "English");
        trailing.setVisibility(View.VISIBLE);
    }

    // ── Moderation policy ─────────────────────────────────────────────────────
    private void openModerationPolicy() {
        BottomSheetDialog d = scaffold(R.drawable.ic_gavel, R.string.adm_mod_policy);
        LinearLayout body = body(d);

        paragraph(body, "These rules apply to every public listing, review and message on "
                + "TourGo. Violations are removed and repeat offenders are suspended.");

        sectionLabel(body, getString(R.string.adm_policy_rules));
        clause(body, "Prohibited content",
                "No hate speech, harassment, explicit material or illegal activity.");
        clause(body, "Authentic reviews",
                "Reviews must reflect a genuine stay or tour — no incentivised or fake reviews.");
        clause(body, "No off-platform promotion",
                "External links and contact details in listings or reviews are removed.");

        sectionLabel(body, getString(R.string.adm_policy_automation));
        toggle(body, "Auto-flag profanity",
                "Hold comments containing flagged words for manual review.", true);
        toggle(body, "Hold new listings",
                "A business's first listing waits for approval before going live.", true);
        toggle(body, "Require photo verification",
                "Businesses must verify cover photos before publishing.", false);

        primaryButton(body, getString(R.string.adm_set_save), R.color.adm_gray_900, R.color.white,
                view -> { d.dismiss(); toast(R.string.adm_toast_policy_saved); });
        d.show();
    }

    // ── Admin team & roles (live: GET /api/admin/team) ─────────────────────────
    private void openAdminTeam() {
        BottomSheetDialog d = scaffold(R.drawable.ic_users, R.string.adm_admin_team);
        LinearLayout body = body(d);
        sectionLabel(body, getString(R.string.adm_team_members));

        // The invite button is added now (synchronously); members are inserted
        // above it as soon as the request returns.
        primaryButton(body, getString(R.string.adm_team_invite), R.color.adm_gray_100, R.color.adm_gray_900,
                view -> { d.dismiss(); toast(R.string.adm_toast_invite_sent); });

        AdminService.getTeam(requireContext(), new DataCallback<List<AdminTeamMember>>() {
            @Override
            public void onSuccess(List<AdminTeamMember> members) {
                if (!isAdded() || members == null) return;
                LayoutInflater inf = LayoutInflater.from(requireContext());
                int pos = 1; // after the section label, before the invite button
                for (AdminTeamMember m : members) {
                    View card = inf.inflate(R.layout.item_admin_team_member, body, false);
                    AdminUi.avatar(card.findViewById(R.id.admMemberAvatar), m.getName());
                    ((TextView) card.findViewById(R.id.admMemberName)).setText(m.getName());
                    ((TextView) card.findViewById(R.id.admMemberEmail)).setText(m.getEmail());
                    TextView chip = card.findViewById(R.id.admMemberRole);
                    chip.setText(m.getRole());
                    styleRoleChip(chip, "owner");
                    body.addView(card, pos++);
                }
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                // Leave the team section empty on failure.
            }
        });

        d.show();
    }

    private void styleRoleChip(TextView chip, String kind) {
        int bg, ink;
        switch (kind) {
            case "owner":   bg = R.color.adm_blue_50;    ink = R.color.adm_blue_700;  break;
            case "support": bg = R.color.adm_purple_100; ink = R.color.adm_purple_500; break;
            case "mod":
            default:        bg = R.color.adm_teal_100;   ink = R.color.adm_teal_ink;  break;
        }
        chip.setBackgroundTintList(ColorStateList.valueOf(color(bg)));
        chip.setTextColor(color(ink));
    }

    // ── Audit log (live: GET /api/admin/audit-log) ─────────────────────────────
    private void openAuditLog() {
        BottomSheetDialog d = scaffold(R.drawable.ic_history, R.string.adm_audit_log);
        LinearLayout body = body(d);
        sectionLabel(body, getString(R.string.adm_audit_recent));

        AdminService.getAuditLog(requireContext(), new DataCallback<List<AdminAuditEntry>>() {
            @Override
            public void onSuccess(List<AdminAuditEntry> logs) {
                if (!isAdded() || logs == null) return;
                LayoutInflater inf = LayoutInflater.from(requireContext());
                for (AdminAuditEntry e : logs) {
                    View card = inf.inflate(R.layout.item_admin_audit, body, false);
                    ((TextView) card.findViewById(R.id.admAuditAction)).setText(e.getAction());
                    ((TextView) card.findViewById(R.id.admAuditMeta)).setText(auditMeta(e));
                    applyAuditIcon(card, e.getKind());
                    body.addView(card);
                }
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                // Leave the audit log empty on failure.
            }
        });

        d.show();
    }

    /** "Actor • 2h" meta line for an audit entry. */
    private String auditMeta(AdminAuditEntry e) {
        String when = AdminMockData.relativeShort(e.getCreatedAt());
        if (e.getActorName().isEmpty()) return when;
        return when.isEmpty() ? e.getActorName() : e.getActorName() + " • " + when;
    }

    private void applyAuditIcon(View card, String kind) {
        int icon, accent, soft;
        switch (kind) {
            case "approve":  icon = R.drawable.ic_check;        accent = R.color.adm_green_500;  soft = R.color.adm_green_100;  break;
            case "suspend":  icon = R.drawable.ic_ban;          accent = R.color.adm_red_500;    soft = R.color.adm_red_100;    break;
            case "reject":   icon = R.drawable.ic_close;        accent = R.color.adm_red_500;    soft = R.color.adm_red_100;    break;
            case "revision": icon = R.drawable.ic_alert_circle; accent = R.color.adm_amber_500;  soft = R.color.adm_amber_100;  break;
            case "role":     icon = R.drawable.ic_users;        accent = R.color.adm_blue_500;   soft = R.color.adm_blue_50;    break;
            case "policy":
            default:         icon = R.drawable.ic_gavel;        accent = R.color.adm_purple_500; soft = R.color.adm_purple_100; break;
        }
        card.findViewById(R.id.admAuditIconWrap)
                .setBackgroundTintList(ColorStateList.valueOf(color(soft)));
        ImageView iv = card.findViewById(R.id.admAuditIcon);
        iv.setImageResource(icon);
        iv.setImageTintList(ColorStateList.valueOf(color(accent)));
    }

    // ── Notifications → role-aware notification center ────────────────────────
    private void openNotifications() {
        Intent i = new Intent(requireContext(), NotificationsActivity.class);
        i.putExtra(NotificationsActivity.EXTRA_ROLE, NotificationMockData.Role.ADMIN.name());
        startActivity(i);
    }

    // ── Language ──────────────────────────────────────────────────────────────
    private void openLanguage() {
        BottomSheetDialog d = scaffold(R.drawable.ic_globe_24, R.string.adm_language);
        LinearLayout body = body(d);
        String current = LocaleHelper.getCurrentLanguageTag();
        languageOption(body, d, "English", "en", !"vi".equals(current));
        languageOption(body, d, "Tiếng Việt", "vi", "vi".equals(current));
        d.show();
    }

    private void languageOption(LinearLayout body, BottomSheetDialog d, String label,
                                String tag, boolean selected) {
        View row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_admin_setting_select, body, false);
        ((TextView) row.findViewById(R.id.admSelectTitle)).setText(label);
        row.findViewById(R.id.admSelectCheck)
                .setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
        row.setOnClickListener(v -> {
            d.dismiss();
            // Only switch if the choice actually changed — setting it recreates the activity.
            if (!tag.equals(LocaleHelper.getCurrentLanguageTag())) {
                LocaleHelper.setAppLocale(tag);
            }
        });
        body.addView(row);
    }

    // ── Password & 2FA ────────────────────────────────────────────────────────
    private void openSecurity() {
        BottomSheetDialog d = scaffold(R.drawable.ic_lock_24, R.string.adm_password_2fa);
        LinearLayout body = body(d);

        sectionLabel(body, getString(R.string.adm_sec_change_password));
        final int pwd = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
        EditText current = field(body, getString(R.string.adm_pwd_current_hint), pwd);
        EditText fresh = field(body, getString(R.string.adm_pwd_new_hint), pwd);
        EditText confirm = field(body, getString(R.string.adm_pwd_confirm_hint), pwd);

        sectionLabel(body, getString(R.string.adm_sec_two_factor));
        SwitchCompat tfa = toggle(body, getString(R.string.adm_2fa_label),
                getString(R.string.adm_2fa_desc), false);
        tfa.setOnCheckedChangeListener((btn, checked) ->
                toast(checked ? R.string.adm_toast_2fa_enabled : R.string.adm_toast_2fa_disabled));

        primaryButton(body, getString(R.string.adm_pwd_update), R.color.adm_gray_900, R.color.white, view -> {
            String now = fresh.getText().toString();
            if (current.getText().toString().trim().isEmpty()) {
                error(current, R.string.adm_pwd_err_current);
            } else if (now.length() < 8) {
                error(fresh, R.string.adm_pwd_err_short);
            } else if (!now.equals(confirm.getText().toString())) {
                error(confirm, R.string.adm_pwd_err_match);
            } else {
                d.dismiss();
                toast(R.string.adm_toast_password_updated);
            }
        });
        d.show();
    }

    private void error(EditText field, @StringRes int msg) {
        field.setError(getString(msg));
        field.requestFocus();
    }

    // ── Help center ───────────────────────────────────────────────────────────
    private void openHelp() {
        BottomSheetDialog d = scaffold(R.drawable.ic_help_circle, R.string.adm_help_center);
        LinearLayout body = body(d);

        sectionLabel(body, getString(R.string.adm_help_topics));
        clause(body, "Reviewing & approving listings",
                "How moderation decisions, revisions and SLAs work.");
        clause(body, "Handling user reports",
                "Escalation paths and the enforcement actions available to you.");
        clause(body, "Managing the admin team",
                "Roles, permissions and how to invite new admins.");

        sectionLabel(body, getString(R.string.adm_help_reach));
        primaryButton(body, getString(R.string.adm_help_contact), R.color.adm_gray_900, R.color.white, view -> {
            d.dismiss();
            Intent email = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@tourgo.app"));
            email.putExtra(Intent.EXTRA_SUBJECT, "TourGo admin support");
            try {
                startActivity(email);
            } catch (ActivityNotFoundException e) {
                toast(R.string.adm_toast_no_mail);
            }
        });
        d.show();
    }

    // ── Logout (unchanged) ────────────────────────────────────────────────────
    private void showLogoutDialog() {
        // Shared centered confirm popup (warning icon + red danger button) — same
        // across Traveler / Business / Admin.
        AdminUi.confirm(requireContext(),
                getString(R.string.profile_logout_title),
                getString(R.string.profile_logout_confirm),
                getString(R.string.profile_logout_title),
                true, this::logout);
    }

    private void logout() {
        new SessionManager(requireContext()).clear();

        // Clear all repository caches on logout (mirrors the user-side ProfileFragment).
        UserRepository.getInstance().clearCache();
        FavoriteRepository.getInstance().clearCache();
        HotelRepository.getInstance().clearCache();
        TourRepository.getInstance().clearCache();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // ── Row binding ───────────────────────────────────────────────────────────
    private void row(View root, int rowId, @DrawableRes int iconRes, @StringRes int labelRes,
                     int trailingRes, Runnable onClick) {
        View row = root.findViewById(rowId);
        ((ImageView) row.findViewById(R.id.admRowIcon)).setImageResource(iconRes);
        ((TextView) row.findViewById(R.id.admRowLabel)).setText(labelRes);
        TextView trailing = row.findViewById(R.id.admRowTrailing);
        if (trailingRes != 0) {
            trailing.setText(trailingRes);
            trailing.setVisibility(View.VISIBLE);
        }
        if (onClick != null) row.setOnClickListener(v -> onClick.run());
    }

    // ── Settings-sheet scaffold + builders ────────────────────────────────────
    /** Inflate the shared settings-sheet scaffold (icon + title + close), expanded on show. */
    private BottomSheetDialog scaffold(@DrawableRes int iconRes, @StringRes int titleRes) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(requireContext())
                .inflate(R.layout.sheet_admin_settings, null);
        dialog.setContentView(sheet);
        ((ImageView) sheet.findViewById(R.id.admSheetIcon)).setImageResource(iconRes);
        ((TextView) sheet.findViewById(R.id.admSheetTitle)).setText(titleRes);
        sheet.findViewById(R.id.admSheetClose).setOnClickListener(view -> dialog.dismiss());
        dialog.setOnShowListener(dlg -> {
            View bs = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bs != null) BottomSheetBehavior.from(bs).setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        return dialog;
    }

    private LinearLayout body(BottomSheetDialog dialog) {
        return dialog.findViewById(R.id.admSheetBody);
    }

    /** Lead paragraph at the top of a sheet. */
    private void paragraph(LinearLayout parent, String text) {
        TextView t = new TextView(requireContext());
        t.setText(text);
        t.setTextColor(color(R.color.adm_gray_600));
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        t.setLineSpacing(0, 1.4f);
        LinearLayout.LayoutParams lp = matchWrap();
        lp.topMargin = dp(6);
        t.setLayoutParams(lp);
        parent.addView(t);
    }

    /** Uppercase section divider, matching the @style/AdmSectionLabel look. */
    private void sectionLabel(LinearLayout parent, CharSequence text) {
        TextView t = new TextView(requireContext());
        t.setText(text);
        t.setAllCaps(true);
        t.setLetterSpacing(0.04f);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(color(R.color.adm_gray_400));
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(14);
        lp.bottomMargin = dp(8);
        t.setLayoutParams(lp);
        parent.addView(t);
    }

    /** Rounded info card with a bold title over a muted description. */
    private void clause(LinearLayout parent, String title, String desc) {
        LinearLayout box = roundedCard();
        TextView t = new TextView(requireContext());
        t.setText(title);
        t.setTextColor(color(R.color.adm_gray_900));
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        box.addView(t);

        TextView dsc = new TextView(requireContext());
        dsc.setText(desc);
        dsc.setTextColor(color(R.color.adm_gray_600));
        dsc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        dsc.setLineSpacing(0, 1.35f);
        LinearLayout.LayoutParams lp = matchWrap();
        lp.topMargin = dp(3);
        dsc.setLayoutParams(lp);
        box.addView(dsc);

        parent.addView(box);
    }

    /** A title/description row with a trailing switch. Returns the switch for wiring. */
    private SwitchCompat toggle(LinearLayout parent, CharSequence title, CharSequence desc, boolean on) {
        LinearLayout row = roundedCard();
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout text = new LinearLayout(requireContext());
        text.setOrientation(LinearLayout.VERTICAL);
        text.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView t = new TextView(requireContext());
        t.setText(title);
        t.setTextColor(color(R.color.adm_gray_900));
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        text.addView(t);

        TextView dsc = new TextView(requireContext());
        dsc.setText(desc);
        dsc.setTextColor(color(R.color.adm_gray_500));
        dsc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        dsc.setLineSpacing(0, 1.3f);
        LinearLayout.LayoutParams dlp = matchWrap();
        dlp.topMargin = dp(2);
        dsc.setLayoutParams(dlp);
        text.addView(dsc);

        row.addView(text);

        SwitchCompat sw = new SwitchCompat(requireContext());
        sw.setChecked(on);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        slp.setMarginStart(dp(10));
        sw.setLayoutParams(slp);
        row.addView(sw);

        parent.addView(row);
        return sw;
    }

    /** Styled password/text field. */
    private EditText field(LinearLayout parent, String hint, int inputType) {
        EditText e = new EditText(requireContext());
        e.setHint(hint);
        e.setInputType(inputType);
        e.setBackgroundResource(R.drawable.bg_adm_rounded_12);
        e.setBackgroundTintList(ColorStateList.valueOf(color(R.color.adm_gray_50)));
        e.setTextColor(color(R.color.adm_gray_900));
        e.setHintTextColor(color(R.color.adm_gray_400));
        e.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        e.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams lp = matchWrap();
        lp.topMargin = dp(8);
        e.setLayoutParams(lp);
        parent.addView(e);
        return e;
    }

    /** Full-width pill action button. */
    private void primaryButton(LinearLayout parent, CharSequence text, @ColorRes int bg,
                               @ColorRes int fg, View.OnClickListener onClick) {
        MaterialButton b = new MaterialButton(requireContext());
        b.setText(text);
        b.setAllCaps(false);
        b.setTextColor(color(fg));
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        b.setBackgroundTintList(ColorStateList.valueOf(color(bg)));
        b.setCornerRadius(dp(999));
        b.setInsetTop(0);
        b.setInsetBottom(0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        lp.topMargin = dp(14);
        b.setLayoutParams(lp);
        b.setOnClickListener(onClick);
        parent.addView(b);
    }

    private LinearLayout roundedCard() {
        LinearLayout box = new LinearLayout(requireContext());
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundResource(R.drawable.bg_adm_rounded_12);
        box.setBackgroundTintList(ColorStateList.valueOf(color(R.color.adm_gray_50)));
        box.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams lp = matchWrap();
        lp.bottomMargin = dp(8);
        box.setLayoutParams(lp);
        return box;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int color(@ColorRes int res) {
        return ContextCompat.getColor(requireContext(), res);
    }

    private int dp(float v) {
        return AdminUi.dp(requireContext(), v);
    }

    private void toast(@StringRes int res) {
        Toast.makeText(requireContext(), res, Toast.LENGTH_SHORT).show();
    }
}
