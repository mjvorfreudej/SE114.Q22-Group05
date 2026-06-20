package com.example.tourgo.ui.admin;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tourgo.R;
import com.example.tourgo.data.local.AdminPreferences;
import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.error.ErrorHandler;
import com.example.tourgo.models.request.UpdatePasswordRequest;
import com.example.tourgo.models.response.AdminAuditEntry;
import com.example.tourgo.models.response.AdminTeamMember;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.AuditExport;
import com.example.tourgo.models.response.ModerationPolicy;
import com.example.tourgo.models.response.NotificationPrefs;
import com.example.tourgo.remote.RetrofitClient;
import com.example.tourgo.remote.service.AdminService;
import com.example.tourgo.ui.main.profile.EditProfileActivity;
import com.example.tourgo.utils.LocaleHelper;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.example.tourgo.data.local.AdminPreferences.NOTIF_DIGEST;
import static com.example.tourgo.data.local.AdminPreferences.NOTIF_PENDING;
import static com.example.tourgo.data.local.AdminPreferences.NOTIF_REPORTED;
import static com.example.tourgo.data.local.AdminPreferences.NOTIF_SECURITY;
import static com.example.tourgo.data.local.AdminPreferences.NOTIF_SLA;
import static com.example.tourgo.data.local.AdminPreferences.NOTIF_TEAM;
import static com.example.tourgo.data.local.AdminPreferences.NOTIF_WEEKLY;
import static com.example.tourgo.data.local.AdminPreferences.POLICY_AUTOHIDE;
import static com.example.tourgo.data.local.AdminPreferences.POLICY_GEO;
import static com.example.tourgo.data.local.AdminPreferences.POLICY_HIDE_AT;
import static com.example.tourgo.data.local.AdminPreferences.POLICY_PHOTO;
import static com.example.tourgo.data.local.AdminPreferences.POLICY_PROFANITY;
import static com.example.tourgo.data.local.AdminPreferences.POLICY_SLA;
import static com.example.tourgo.data.local.AdminPreferences.POLICY_TERMS;
import static com.example.tourgo.data.local.AdminPreferences.TFA_ENABLED;

/**
 * Admin › Settings detail screens — the seven destinations behind the Admin
 * Settings list (Moderation policy · Admin team & roles · Audit log ·
 * Notifications · Language · Password & 2FA · Help center).
 *
 * <p>A single host that inflates one of seven full-screen layouts based on
 * {@link #EXTRA_SCREEN} and wires its interactions. Implements the
 * "TourGo Admin Detail Screens" design hand-off (AdminDetail.jsx): cool-gray
 * canvas, white r20 cards, SwitchCompat toggles, steppers and one black CTA.
 * Team & audit content is loaded live from the admin REST API.
 */
public class AdminDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SCREEN = "extra_screen";
    public static final String SCREEN_MOD_POLICY = "mod_policy";
    public static final String SCREEN_TEAM = "team";
    public static final String SCREEN_AUDIT = "audit";
    public static final String SCREEN_NOTIFICATIONS = "notifications";
    public static final String SCREEN_LANGUAGE = "language";
    public static final String SCREEN_SECURITY = "security";
    public static final String SCREEN_HELP = "help";

    // ── Audit state ───────────────────────────────────────────────────────────
    private static final int AUDIT_PAGE = 20;
    private final String[] auditKeys = {"all", "approval", "lock", "settings"};
    private TextView[] auditChips;
    private List<AdminAuditEntry> auditEntries = new ArrayList<>();
    private String auditFilter = "all";
    private View auditRoot;
    private boolean auditHasMore = true;
    private boolean auditLoading;

    // ── Help (FAQ accordion) state ──────────────────────────────────────────────
    private Chip policyAddChip;
    private int openFaq = -1;

    /** Local store for settings that have no backend endpoint (policy / notif / 2FA). */
    private AdminPreferences prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        prefs = new AdminPreferences(this);

        String screen = getIntent().getStringExtra(EXTRA_SCREEN);
        if (screen == null) screen = SCREEN_MOD_POLICY;

        View root = getLayoutInflater().inflate(layoutFor(screen), null);
        setContentView(root);

        root.findViewById(R.id.admDetailBack).setOnClickListener(v -> finish());
        ((TextView) root.findViewById(R.id.admDetailTitle)).setText(titleFor(screen));
        applyInsets(root);

        switch (screen) {
            case SCREEN_TEAM:          bindTeam(root); break;
            case SCREEN_AUDIT:         bindAudit(root); break;
            case SCREEN_NOTIFICATIONS: bindNotifications(root); break;
            case SCREEN_LANGUAGE:      bindLanguage(root); break;
            case SCREEN_SECURITY:      bindSecurity(root); break;
            case SCREEN_HELP:          bindHelp(root); break;
            case SCREEN_MOD_POLICY:
            default:                   bindModPolicy(root); break;
        }
    }

    @LayoutRes
    private int layoutFor(String screen) {
        switch (screen) {
            case SCREEN_TEAM:          return R.layout.screen_admin_team;
            case SCREEN_AUDIT:         return R.layout.screen_admin_audit;
            case SCREEN_NOTIFICATIONS: return R.layout.screen_admin_notif_prefs;
            case SCREEN_LANGUAGE:      return R.layout.screen_admin_language;
            case SCREEN_SECURITY:      return R.layout.screen_admin_security;
            case SCREEN_HELP:          return R.layout.screen_admin_help;
            case SCREEN_MOD_POLICY:
            default:                   return R.layout.screen_admin_mod_policy;
        }
    }

    @StringRes
    private int titleFor(String screen) {
        switch (screen) {
            case SCREEN_TEAM:          return R.string.adm_admin_team;
            case SCREEN_AUDIT:         return R.string.adm_audit_log;
            case SCREEN_NOTIFICATIONS: return R.string.adm_notifications;
            case SCREEN_LANGUAGE:      return R.string.adm_language;
            case SCREEN_SECURITY:      return R.string.adm_password_2fa;
            case SCREEN_HELP:          return R.string.adm_help_center;
            case SCREEN_MOD_POLICY:
            default:                   return R.string.adm_mod_policy;
        }
    }

    /** Status-bar inset → detail bar top; nav-bar inset → bottom-most child. */
    private void applyInsets(View root) {
        final View bar = root.findViewById(R.id.admDetailBar);
        final int baseTop = bar.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(bar, (v, insets) -> {
            Insets b = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), baseTop + b.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        ViewGroup rootGroup = (ViewGroup) root;
        final View last = rootGroup.getChildAt(rootGroup.getChildCount() - 1);
        final int baseBottom = last.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(last, (v, insets) -> {
            Insets b = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), baseBottom + b.bottom);
            return insets;
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // 1 · MODERATION POLICY
    // ════════════════════════════════════════════════════════════════════════
    private void bindModPolicy(View root) {
        // Load from the server; fall back to the local cache if it's unreachable.
        AdminService.getModerationPolicy(this, new DataCallback<ModerationPolicy>() {
            @Override
            public void onSuccess(ModerationPolicy policy) {
                if (isFinishing() || policy == null) return;
                cachePolicy(policy);
                applyPolicy(root, policy);
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (isFinishing()) return;
                applyPolicy(root, localPolicy());
            }
        });
    }

    private void applyPolicy(View root, ModerationPolicy p) {
        final SwitchCompat autoHide = root.findViewById(R.id.admPolicyAutohide);
        final SwitchCompat profanity = root.findViewById(R.id.admPolicyProfanity);
        final SwitchCompat photo = root.findViewById(R.id.admPolicyPhoto);
        final SwitchCompat geo = root.findViewById(R.id.admPolicyGeo);
        autoHide.setChecked(p.isAutoHide());
        profanity.setChecked(p.isProfanity());
        photo.setChecked(p.isPhotoReview());
        geo.setChecked(p.isGeoBlock());

        final int[] hideAt = {p.getHideAt() > 0 ? p.getHideAt() : 5};
        final int[] sla = {p.getSlaHours() > 0 ? p.getSlaHours() : 24};
        wireStepper(root, R.id.admHideAtMinus, R.id.admHideAtValue, R.id.admHideAtPlus,
                hideAt, 1, 20, null);
        wireStepper(root, R.id.admSlaMinus, R.id.admSlaValue, R.id.admSlaPlus,
                sla, 4, 72, getString(R.string.adm_hours_suffix));

        final ChipGroup terms = root.findViewById(R.id.admPolicyTerms);
        terms.removeAllViews();
        policyAddChip = null;
        List<String> seed = p.getTerms().isEmpty()
                ? Arrays.asList("scam", "counterfeit", "unsafe") : p.getTerms();
        bindTerms(terms, seed);

        root.findViewById(R.id.admPolicySave).setOnClickListener(v -> {
            ModerationPolicy out = new ModerationPolicy(
                    autoHide.isChecked(), profanity.isChecked(), photo.isChecked(), geo.isChecked(),
                    hideAt[0], sla[0], currentTerms(terms));
            cachePolicy(out); // cache immediately so the change is never lost
            AdminService.updateModerationPolicy(AdminDetailActivity.this, out, new DataCallback<Void>() {
                @Override public void onSuccess(Void unused) {
                    toast(R.string.adm_toast_policy_saved);
                    finish();
                }
                @Override public void onError(ApiErrorCode code, String msg) {
                    toastMsg(msg);
                }
            });
        });
    }

    private void cachePolicy(ModerationPolicy p) {
        prefs.setBool(POLICY_AUTOHIDE, p.isAutoHide());
        prefs.setBool(POLICY_PROFANITY, p.isProfanity());
        prefs.setBool(POLICY_PHOTO, p.isPhotoReview());
        prefs.setBool(POLICY_GEO, p.isGeoBlock());
        prefs.setInt(POLICY_HIDE_AT, p.getHideAt() > 0 ? p.getHideAt() : 5);
        prefs.setInt(POLICY_SLA, p.getSlaHours() > 0 ? p.getSlaHours() : 24);
        prefs.setList(POLICY_TERMS, p.getTerms());
    }

    private ModerationPolicy localPolicy() {
        return new ModerationPolicy(
                prefs.getBool(POLICY_AUTOHIDE, true),
                prefs.getBool(POLICY_PROFANITY, true),
                prefs.getBool(POLICY_PHOTO, false),
                prefs.getBool(POLICY_GEO, false),
                prefs.getInt(POLICY_HIDE_AT, 5),
                prefs.getInt(POLICY_SLA, 24),
                prefs.getList(POLICY_TERMS, Arrays.asList("scam", "counterfeit", "unsafe")));
    }

    /** Wire a ± stepper backed by a caller-owned cell, so the live value is
     *  readable at save time. Clamps the initial value into [min, max]. */
    private void wireStepper(View root, int minusId, int valueId, int plusId,
                             int[] v, int min, int max, @Nullable String suffix) {
        final ImageView minus = root.findViewById(minusId);
        final TextView value = root.findViewById(valueId);
        final ImageView plus = root.findViewById(plusId);
        v[0] = Math.max(min, Math.min(max, v[0]));
        final Runnable render = () -> {
            value.setText(suffix == null ? String.valueOf(v[0]) : v[0] + " " + suffix);
            stepEnabled(minus, v[0] > min);
            stepEnabled(plus, v[0] < max);
        };
        minus.setOnClickListener(view -> { if (v[0] > min) { v[0]--; render.run(); } });
        plus.setOnClickListener(view -> { if (v[0] < max) { v[0]++; render.run(); } });
        render.run();
    }

    private void stepEnabled(ImageView btn, boolean enabled) {
        btn.setEnabled(enabled);
        btn.setClickable(enabled);
        btn.setAlpha(enabled ? 1f : 0.4f);
    }

    private void bindTerms(ChipGroup group, List<String> initialTerms) {
        policyAddChip = new Chip(this);
        policyAddChip.setText(R.string.adm_policy_add_term);
        policyAddChip.setTextColor(color(R.color.adm_blue_500));
        policyAddChip.setChipBackgroundColor(ColorStateList.valueOf(color(R.color.white)));
        policyAddChip.setChipStrokeColor(ColorStateList.valueOf(color(R.color.adm_gray_300)));
        policyAddChip.setChipStrokeWidth(dp(1));
        policyAddChip.setChipIcon(ContextCompat.getDrawable(this, R.drawable.ic_plus));
        policyAddChip.setChipIconTint(ColorStateList.valueOf(color(R.color.adm_blue_500)));
        policyAddChip.setChipIconSize(dp(12));
        policyAddChip.setEnsureMinTouchTargetSize(false);
        policyAddChip.setChipMinHeight(dp(30));
        policyAddChip.setOnClickListener(v -> showAddTermDialog(group));

        for (String w : initialTerms) addTermChip(group, w);
        group.addView(policyAddChip);
    }

    /** Current term chips (in order), excluding the trailing "Add term" chip. */
    private List<String> currentTerms(ChipGroup group) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child == policyAddChip || !(child instanceof Chip)) continue;
            out.add(((Chip) child).getText().toString());
        }
        return out;
    }

    private void addTermChip(ChipGroup group, String term) {
        Chip chip = new Chip(this);
        chip.setText(term);
        chip.setTextColor(color(R.color.adm_gray_700));
        chip.setChipBackgroundColor(ColorStateList.valueOf(color(R.color.adm_gray_100)));
        chip.setChipStrokeWidth(0);
        chip.setCloseIconVisible(true);
        chip.setCloseIcon(ContextCompat.getDrawable(this, R.drawable.ic_close));
        chip.setCloseIconTint(ColorStateList.valueOf(color(R.color.adm_gray_500)));
        chip.setEnsureMinTouchTargetSize(false);
        chip.setChipMinHeight(dp(30));
        chip.setOnCloseIconClickListener(v -> group.removeView(chip));
        int idx = policyAddChip != null ? group.indexOfChild(policyAddChip) : group.getChildCount();
        if (idx < 0) idx = group.getChildCount();
        group.addView(chip, idx);
    }

    private void showAddTermDialog(ChipGroup group) {
        final EditText input = new EditText(this);
        input.setHint(R.string.adm_policy_add_term_hint);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        int pad = dp(20);
        new AlertDialog.Builder(this)
                .setTitle(R.string.adm_policy_add_term)
                .setView(input)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    String t = input.getText().toString().trim();
                    if (!t.isEmpty()) addTermChip(group, t);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        input.setPadding(pad, dp(8), pad, dp(8));
    }

    // ════════════════════════════════════════════════════════════════════════
    // 2 · ADMIN TEAM & ROLES  (live: GET /api/admin/team)
    // ════════════════════════════════════════════════════════════════════════
    private void bindTeam(View root) {
        final LinearLayout container = root.findViewById(R.id.admTeamMembers);
        final TextView summary = root.findViewById(R.id.admTeamSummary);
        root.findViewById(R.id.admTeamInvite)
                .setOnClickListener(v -> showInviteDialog(container, summary));
        loadTeam(container, summary);
    }

    private void loadTeam(LinearLayout container, TextView summary) {
        final String myEmail = new SessionManager(this).getEmail();
        AdminService.getTeam(this, new DataCallback<List<AdminTeamMember>>() {
            @Override
            public void onSuccess(List<AdminTeamMember> members) {
                if (isFinishing() || members == null) return;
                container.removeAllViews();
                summary.setText(getString(R.string.adm_team_summary, members.size()));
                LayoutInflater inf = LayoutInflater.from(AdminDetailActivity.this);
                for (int i = 0; i < members.size(); i++) {
                    AdminTeamMember m = members.get(i);
                    View row = inf.inflate(R.layout.item_admin_team_member, container, false);
                    AdminUi.avatar(row.findViewById(R.id.admMemberAvatar), m.getName());
                    ((TextView) row.findViewById(R.id.admMemberName)).setText(m.getName());
                    ((TextView) row.findViewById(R.id.admMemberEmail)).setText(m.getEmail());
                    boolean isYou = myEmail != null && myEmail.equalsIgnoreCase(m.getEmail());
                    row.findViewById(R.id.admMemberYou).setVisibility(isYou ? View.VISIBLE : View.GONE);
                    TextView role = row.findViewById(R.id.admMemberRole);
                    role.setText(m.getRole().toUpperCase(Locale.ROOT));
                    styleRoleBadge(role, m.getRole());
                    row.findViewById(R.id.admMemberMenu)
                            .setOnClickListener(v -> showMemberMenu(v, m, isYou, container, summary));
                    container.addView(row);
                    if (i < members.size() - 1) addDivider(container);
                }
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                // Leave the members card empty on failure.
            }
        });
    }

    private void styleRoleBadge(TextView chip, String role) {
        String r = role.toLowerCase(Locale.ROOT);
        int bg, fg;
        if (r.contains("owner")) {
            bg = R.color.adm_blue_700; fg = R.color.white;
        } else if (r.contains("mod")) {
            bg = R.color.adm_amber_100; fg = R.color.adm_amber_700;
        } else if (r.contains("admin")) {
            bg = R.color.adm_blue_50; fg = R.color.adm_blue_700;
        } else {
            bg = R.color.adm_gray_200; fg = R.color.adm_gray_700;
        }
        chip.setBackgroundTintList(ColorStateList.valueOf(color(bg)));
        chip.setTextColor(color(fg));
    }

    private void showMemberMenu(View anchor, AdminTeamMember member, boolean isYou,
                                LinearLayout container, TextView summary) {
        PopupMenu menu = new PopupMenu(this, anchor);
        if (isYou) {
            menu.getMenu().add(R.string.adm_team_edit_profile);
            menu.setOnMenuItemClickListener(item -> {
                startActivity(new Intent(AdminDetailActivity.this, EditProfileActivity.class));
                return true;
            });
        } else {
            menu.getMenu().add(0, 1, 0, R.string.adm_team_change_role);
            menu.getMenu().add(0, 2, 1, R.string.adm_team_remove);
            menu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) showChangeRoleDialog(member, container, summary);
                else if (item.getItemId() == 2) confirmRemoveAdmin(member, container, summary);
                return true;
            });
        }
        menu.show();
    }

    private void showInviteDialog(LinearLayout container, TextView summary) {
        final EditText input = new EditText(this);
        input.setHint(R.string.adm_team_invite_hint);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        int pad = dp(20);
        new AlertDialog.Builder(this)
                .setTitle(R.string.adm_team_invite_admin)
                .setView(input)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    String email = input.getText().toString().trim();
                    if (email.isEmpty()) return;
                    AdminService.inviteAdmin(AdminDetailActivity.this, email, new DataCallback<AdminTeamMember>() {
                        @Override public void onSuccess(AdminTeamMember m) {
                            toast(R.string.adm_toast_invite_sent);
                            loadTeam(container, summary);
                        }
                        @Override public void onError(ApiErrorCode code, String msg) {
                            toastMsg(msg);
                        }
                    });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        input.setPadding(pad, dp(8), pad, dp(8));
    }

    private void showChangeRoleDialog(AdminTeamMember member, LinearLayout container, TextView summary) {
        final String[] roles = {"OWNER", "ADMIN", "MODERATOR"};
        new AlertDialog.Builder(this)
                .setTitle(R.string.adm_team_change_role)
                .setItems(roles, (d, which) -> AdminService.changeAdminRole(
                        AdminDetailActivity.this, member.getId(), roles[which], new DataCallback<Void>() {
                            @Override public void onSuccess(Void unused) {
                                toast(R.string.adm_toast_role_changed);
                                loadTeam(container, summary);
                            }
                            @Override public void onError(ApiErrorCode code, String msg) {
                                toastMsg(msg);
                            }
                        }))
                .show();
    }

    private void confirmRemoveAdmin(AdminTeamMember member, LinearLayout container, TextView summary) {
        AdminUi.confirm(this,
                getString(R.string.adm_team_remove),
                getString(R.string.adm_team_remove_confirm, member.getName()),
                getString(R.string.adm_team_remove),
                true,
                () -> AdminService.removeAdmin(AdminDetailActivity.this, member.getId(), new DataCallback<Void>() {
                    @Override public void onSuccess(Void unused) {
                        toast(R.string.adm_toast_admin_removed);
                        loadTeam(container, summary);
                    }
                    @Override public void onError(ApiErrorCode code, String msg) {
                        toastMsg(msg);
                    }
                }));
    }

    // ════════════════════════════════════════════════════════════════════════
    // 3 · AUDIT LOG  (live: GET /api/admin/audit-log)
    // ════════════════════════════════════════════════════════════════════════
    private void bindAudit(View root) {
        auditRoot = root;
        View action = root.findViewById(R.id.admDetailAction);
        action.setVisibility(View.VISIBLE);
        ((ImageView) root.findViewById(R.id.admDetailActionIcon))
                .setImageResource(R.drawable.ic_download);
        action.setOnClickListener(v -> exportAudit());

        auditChips = new TextView[]{
                root.findViewById(R.id.admChipAll),
                root.findViewById(R.id.admChipApprovals),
                root.findViewById(R.id.admChipLocks),
                root.findViewById(R.id.admChipSettings)};
        for (int i = 0; i < auditChips.length; i++) {
            final String key = auditKeys[i];
            auditChips[i].setOnClickListener(v -> {
                auditFilter = key;
                styleAuditChips();
                renderAudit();
            });
        }
        styleAuditChips();

        AdminService.getAuditLog(this, null, AUDIT_PAGE, new DataCallback<List<AdminAuditEntry>>() {
            @Override
            public void onSuccess(List<AdminAuditEntry> logs) {
                if (isFinishing() || logs == null) return;
                auditEntries = new ArrayList<>(logs);
                auditHasMore = logs.size() >= AUDIT_PAGE;
                renderAudit();
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                // Leave the timeline empty on failure.
            }
        });
    }

    /** Fetch the next page of older entries (cursor = oldest loaded created_at). */
    private void loadMoreAudit() {
        if (auditLoading || auditEntries.isEmpty()) return;
        auditLoading = true;
        String oldest = auditEntries.get(auditEntries.size() - 1).getCreatedAt();
        AdminService.getAuditLog(this, oldest, AUDIT_PAGE, new DataCallback<List<AdminAuditEntry>>() {
            @Override
            public void onSuccess(List<AdminAuditEntry> logs) {
                auditLoading = false;
                if (isFinishing() || logs == null) return;
                auditHasMore = logs.size() >= AUDIT_PAGE;
                if (logs.isEmpty()) {
                    toast(R.string.adm_audit_no_more);
                } else {
                    auditEntries.addAll(logs);
                }
                renderAudit();
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                auditLoading = false;
                toastMsg(msg);
            }
        });
    }

    /** Export the audit log as CSV (built server-side) and offer a share sheet. */
    private void exportAudit() {
        AdminService.exportAuditLog(this, new DataCallback<AuditExport>() {
            @Override
            public void onSuccess(AuditExport export) {
                if (isFinishing() || export == null || export.getCsv() == null) return;
                shareCsv(export.getFilename() != null ? export.getFilename() : "tourgo-audit-log.csv",
                        export.getCsv());
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                toastMsg(msg);
            }
        });
    }

    /** Write the CSV to cache and share it as a real .csv file via FileProvider. */
    private void shareCsv(String filename, String csv) {
        try {
            File dir = new File(getCacheDir(), "exports");
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            File file = new File(dir, filename);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(csv);
            }
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/csv");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.putExtra(Intent.EXTRA_SUBJECT, filename);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, getString(R.string.adm_audit_export)));
        } catch (IOException e) {
            toastMsg(e.getMessage());
        }
    }

    private void styleAuditChips() {
        for (int i = 0; i < auditChips.length; i++) {
            boolean sel = auditKeys[i].equals(auditFilter);
            auditChips[i].setBackgroundTintList(ColorStateList.valueOf(
                    color(sel ? R.color.adm_gray_900 : R.color.adm_gray_100)));
            auditChips[i].setTextColor(color(sel ? R.color.white : R.color.adm_gray_500));
        }
    }

    private void renderAudit() {
        LinearLayout container = auditRoot.findViewById(R.id.admAuditContainer);
        container.removeAllViews();

        LinkedHashMap<String, List<AdminAuditEntry>> groups = new LinkedHashMap<>();
        for (AdminAuditEntry e : auditEntries) {
            if (!matchesFilter(e.getKind())) continue;
            String day = dayLabel(e.getCreatedAt());
            List<AdminAuditEntry> bucket = groups.get(day);
            if (bucket == null) {
                bucket = new ArrayList<>();
                groups.put(day, bucket);
            }
            bucket.add(e);
        }

        if (groups.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.adm_audit_empty);
            empty.setTextColor(color(R.color.adm_gray_400));
            empty.setTextSize(12.5f);
            empty.setPadding(0, dp(40), 0, 0);
            empty.setGravity(android.view.Gravity.CENTER);
            container.addView(empty);
            return;
        }

        LayoutInflater inf = LayoutInflater.from(this);
        for (Map.Entry<String, List<AdminAuditEntry>> g : groups.entrySet()) {
            container.addView(groupLabel(g.getKey()));

            MaterialCardView card = newCard();
            LinearLayout inner = new LinearLayout(this);
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setPadding(dp(14), dp(4), dp(14), dp(4));
            card.addView(inner);

            List<AdminAuditEntry> items = g.getValue();
            for (int i = 0; i < items.size(); i++) {
                AdminAuditEntry e = items.get(i);
                View tl = inf.inflate(R.layout.item_admin_audit_timeline, inner, false);
                applyTimelineKind(tl, e.getKind());
                ((TextView) tl.findViewById(R.id.admTlText)).setText(buildSentence(e));
                ((TextView) tl.findViewById(R.id.admTlMeta))
                        .setText(g.getKey() + " · " + timeOf(e.getCreatedAt()));
                if (i == items.size() - 1) {
                    tl.findViewById(R.id.admTlConnector).setVisibility(View.INVISIBLE);
                }
                inner.addView(tl);
            }
            container.addView(card);
        }

        if (auditHasMore) {
            TextView more = new TextView(this);
            more.setText(R.string.adm_audit_load_earlier);
            more.setTextColor(color(R.color.adm_blue_500));
            more.setTextSize(12f);
            more.setTypeface(Typeface.DEFAULT_BOLD);
            more.setGravity(android.view.Gravity.CENTER);
            more.setPadding(0, dp(14), 0, dp(4));
            more.setOnClickListener(v -> loadMoreAudit());
            container.addView(more);
        }
    }

    private boolean matchesFilter(String kind) {
        switch (auditFilter) {
            case "approval": return kind.equals("approve") || kind.equals("reject");
            case "lock":     return kind.equals("suspend");
            case "settings": return kind.equals("role") || kind.equals("policy") || kind.equals("revision");
            case "all":
            default:         return true;
        }
    }

    private void applyTimelineKind(View tl, String kind) {
        int icon, accent, soft;
        switch (kind) {
            case "approve":  icon = R.drawable.ic_check_circle;  accent = R.color.adm_green_700; soft = R.color.adm_green_100; break;
            case "reject":   icon = R.drawable.ic_close;         accent = R.color.adm_red_700;   soft = R.color.adm_red_100;   break;
            case "suspend":  icon = R.drawable.ic_shield_x;      accent = R.color.adm_red_700;   soft = R.color.adm_red_100;   break;
            case "role":     icon = R.drawable.ic_users;         accent = R.color.adm_blue_700;  soft = R.color.adm_blue_50;   break;
            case "revision": icon = R.drawable.ic_alert_circle;  accent = R.color.adm_amber_700; soft = R.color.adm_amber_100; break;
            case "content":  icon = R.drawable.ic_eye_open;      accent = R.color.adm_gray_700;  soft = R.color.adm_gray_100;  break;
            case "policy":
            default:         icon = R.drawable.ic_settings;      accent = R.color.adm_blue_700;  soft = R.color.adm_blue_50;   break;
        }
        tl.findViewById(R.id.admTlIconWrap)
                .setBackgroundTintList(ColorStateList.valueOf(color(soft)));
        ImageView iv = tl.findViewById(R.id.admTlIcon);
        iv.setImageResource(icon);
        iv.setImageTintList(ColorStateList.valueOf(color(accent)));
    }

    private CharSequence buildSentence(AdminAuditEntry e) {
        String actor = e.getActorName();
        String action = e.getAction();
        if (actor.isEmpty()) return action;
        SpannableStringBuilder sb = new SpannableStringBuilder(actor);
        sb.setSpan(new StyleSpan(Typeface.BOLD), 0, actor.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(color(R.color.adm_gray_900)), 0, actor.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.append(" ").append(action);
        return sb;
    }

    /** "Today" / "Yesterday" / yyyy-MM-dd, derived from a UTC ISO timestamp. */
    private String dayLabel(String iso) {
        if (iso == null || iso.length() < 10) return "—";
        String date = iso.substring(0, 10);
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        long now = System.currentTimeMillis();
        if (date.equals(f.format(new Date(now)))) return getString(R.string.adm_audit_today);
        if (date.equals(f.format(new Date(now - 86400000L)))) return getString(R.string.adm_audit_yesterday);
        return date;
    }

    private String timeOf(String iso) {
        return (iso != null && iso.length() >= 16) ? iso.substring(11, 16) : "";
    }

    // ════════════════════════════════════════════════════════════════════════
    // 4 · NOTIFICATIONS (preferences)
    // ════════════════════════════════════════════════════════════════════════
    private void bindNotifications(View root) {
        // Load from the server; fall back to the local cache if it's unreachable.
        AdminService.getNotificationPrefs(this, new DataCallback<NotificationPrefs>() {
            @Override
            public void onSuccess(NotificationPrefs p) {
                if (isFinishing() || p == null) return;
                cacheNotif(p);
                applyNotif(root, p);
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (isFinishing()) return;
                applyNotif(root, localNotif());
            }
        });
    }

    private void applyNotif(View root, NotificationPrefs p) {
        SwitchCompat pending = root.findViewById(R.id.admNotifPending);
        SwitchCompat reported = root.findViewById(R.id.admNotifReported);
        SwitchCompat team = root.findViewById(R.id.admNotifTeam);
        SwitchCompat sla = root.findViewById(R.id.admNotifSla);
        SwitchCompat digest = root.findViewById(R.id.admNotifDigest);
        SwitchCompat weekly = root.findViewById(R.id.admNotifWeekly);
        SwitchCompat security = root.findViewById(R.id.admNotifSecurity);

        // Set state first, then attach listeners, so initialisation doesn't trigger a save.
        pending.setChecked(p.isPending());
        reported.setChecked(p.isReported());
        team.setChecked(p.isTeam());
        sla.setChecked(p.isSla());
        digest.setChecked(p.isDigest());
        weekly.setChecked(p.isWeekly());
        security.setChecked(true);
        security.setEnabled(false); // locked on for owners (design footnote)

        CompoundButton.OnCheckedChangeListener saver = (b, checked) -> persistNotif(root);
        pending.setOnCheckedChangeListener(saver);
        reported.setOnCheckedChangeListener(saver);
        team.setOnCheckedChangeListener(saver);
        sla.setOnCheckedChangeListener(saver);
        digest.setOnCheckedChangeListener(saver);
        weekly.setOnCheckedChangeListener(saver);
    }

    /** Build prefs from the current switches; cache locally and sync to the server. */
    private void persistNotif(View root) {
        NotificationPrefs p = new NotificationPrefs(
                ((SwitchCompat) root.findViewById(R.id.admNotifPending)).isChecked(),
                ((SwitchCompat) root.findViewById(R.id.admNotifReported)).isChecked(),
                ((SwitchCompat) root.findViewById(R.id.admNotifTeam)).isChecked(),
                ((SwitchCompat) root.findViewById(R.id.admNotifSla)).isChecked(),
                ((SwitchCompat) root.findViewById(R.id.admNotifDigest)).isChecked(),
                ((SwitchCompat) root.findViewById(R.id.admNotifWeekly)).isChecked(),
                true);
        cacheNotif(p);
        AdminService.updateNotificationPrefs(this, p, new DataCallback<Void>() {
            @Override public void onSuccess(Void unused) { }
            @Override public void onError(ApiErrorCode code, String msg) { }
        });
    }

    private void cacheNotif(NotificationPrefs p) {
        prefs.setBool(NOTIF_PENDING, p.isPending());
        prefs.setBool(NOTIF_REPORTED, p.isReported());
        prefs.setBool(NOTIF_TEAM, p.isTeam());
        prefs.setBool(NOTIF_SLA, p.isSla());
        prefs.setBool(NOTIF_DIGEST, p.isDigest());
        prefs.setBool(NOTIF_WEEKLY, p.isWeekly());
        prefs.setBool(NOTIF_SECURITY, true);
    }

    private NotificationPrefs localNotif() {
        return new NotificationPrefs(
                prefs.getBool(NOTIF_PENDING, true),
                prefs.getBool(NOTIF_REPORTED, true),
                prefs.getBool(NOTIF_TEAM, false),
                prefs.getBool(NOTIF_SLA, true),
                prefs.getBool(NOTIF_DIGEST, true),
                prefs.getBool(NOTIF_WEEKLY, false),
                true);
    }

    // ════════════════════════════════════════════════════════════════════════
    // 5 · LANGUAGE
    // ════════════════════════════════════════════════════════════════════════
    private void bindLanguage(View root) {
        final boolean vi = "vi".equals(LocaleHelper.getCurrentLanguageTag());
        root.findViewById(R.id.admLangEnDisc).setVisibility(vi ? View.GONE : View.VISIBLE);
        root.findViewById(R.id.admLangViDisc).setVisibility(vi ? View.VISIBLE : View.GONE);
        ((TextView) root.findViewById(R.id.admLangEnLabel))
                .setTypeface(null, vi ? Typeface.NORMAL : Typeface.BOLD);
        ((TextView) root.findViewById(R.id.admLangViLabel))
                .setTypeface(null, vi ? Typeface.BOLD : Typeface.NORMAL);

        // Only switch when the choice actually changes — it recreates the activity.
        root.findViewById(R.id.admLangEn).setOnClickListener(v -> {
            if (vi) LocaleHelper.setAppLocale("en");
        });
        root.findViewById(R.id.admLangVi).setOnClickListener(v -> {
            if (!vi) LocaleHelper.setAppLocale("vi");
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // 6 · PASSWORD & 2FA
    // ════════════════════════════════════════════════════════════════════════
    private void bindSecurity(View root) {
        final EditText current = root.findViewById(R.id.admPwdCurrent);
        final EditText fresh = root.findViewById(R.id.admPwdNew);
        final EditText confirm = root.findViewById(R.id.admPwdConfirm);
        final ImageView eye = root.findViewById(R.id.admPwdEye);
        final boolean[] shown = {false};
        eye.setOnClickListener(v -> {
            shown[0] = !shown[0];
            current.setInputType(InputType.TYPE_CLASS_TEXT | (shown[0]
                    ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_TEXT_VARIATION_PASSWORD));
            current.setSelection(current.getText().length());
            eye.setImageResource(shown[0] ? R.drawable.ic_eye_closed : R.drawable.ic_eye_open);
        });

        final View update = root.findViewById(R.id.admPwdUpdate);
        update.setOnClickListener(v -> {
            String now = fresh.getText().toString();
            if (current.getText().toString().trim().isEmpty()) {
                current.setError(getString(R.string.adm_pwd_err_current));
                current.requestFocus();
            } else if (now.length() < 8) {
                fresh.setError(getString(R.string.adm_pwd_err_short));
                fresh.requestFocus();
            } else if (!now.equals(confirm.getText().toString())) {
                confirm.setError(getString(R.string.adm_pwd_err_match));
                confirm.requestFocus();
            } else {
                updatePassword(update, now);
            }
        });

        final SwitchCompat tfa = root.findViewById(R.id.admTfaSwitch);
        final FrameLayout tile = root.findViewById(R.id.admTfaTile);
        final ImageView tileIcon = root.findViewById(R.id.admTfaIcon);
        final TextView sub = root.findViewById(R.id.admTfaSub);
        final View factors = root.findViewById(R.id.admTfaFactors);
        boolean tfaOn = prefs.getBool(TFA_ENABLED, true);
        tfa.setChecked(tfaOn);
        applyTfaState(tfaOn, tile, tileIcon, sub, factors);
        tfa.setOnCheckedChangeListener((b, checked) -> {
            applyTfaState(checked, tile, tileIcon, sub, factors);
            prefs.setBool(TFA_ENABLED, checked);
            toast(checked ? R.string.adm_toast_2fa_enabled : R.string.adm_toast_2fa_disabled);
        });
    }

    /** Change the signed-in admin's password via POST /api/auth/update-password. */
    private void updatePassword(View button, String newPassword) {
        button.setEnabled(false);
        RetrofitClient.getInstance(this)
                .getAuthApi()
                .updatePassword(new UpdatePasswordRequest(newPassword))
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        ApiResponse<Void> body = response.body();
                        if (response.isSuccessful() && body != null
                                && body.getSuccess() != null && body.getSuccess()) {
                            toast(R.string.adm_toast_password_updated);
                            finish();
                        } else {
                            button.setEnabled(true);
                            ErrorHandler.showError(AdminDetailActivity.this,
                                    ErrorHandler.parseError(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        button.setEnabled(true);
                        ErrorHandler.showError(AdminDetailActivity.this, ErrorHandler.parseError(t));
                    }
                });
    }

    /** Reflect the 2FA on/off state in the tile tint, subtitle and factor list. */
    private void applyTfaState(boolean on, FrameLayout tile, ImageView icon, TextView sub, View factors) {
        tile.setBackgroundTintList(ColorStateList.valueOf(
                color(on ? R.color.adm_green_100 : R.color.adm_gray_100)));
        icon.setImageTintList(ColorStateList.valueOf(
                color(on ? R.color.adm_green_700 : R.color.adm_gray_700)));
        sub.setText(on ? R.string.adm_2fa_on_sub : R.string.adm_2fa_off_sub);
        factors.setVisibility(on ? View.VISIBLE : View.GONE);
    }

    // ════════════════════════════════════════════════════════════════════════
    // 7 · HELP CENTER
    // ════════════════════════════════════════════════════════════════════════
    private void bindHelp(View root) {
        setTopic(root, R.id.admTopicModeration, R.drawable.ic_gavel, R.string.adm_help_topic_moderation, 12);
        setTopic(root, R.id.admTopicBusinesses, R.drawable.ic_building, R.string.adm_help_topic_businesses, 9);
        setTopic(root, R.id.admTopicSecurity, R.drawable.ic_lock_24, R.string.adm_help_topic_security, 6);
        setTopic(root, R.id.admTopicTeam, R.drawable.ic_users, R.string.adm_help_topic_team, 5);

        buildFaqs(root);

        root.findViewById(R.id.admHelpContact).setOnClickListener(v -> contactSupport());
    }

    private void setTopic(View root, int includeId, @DrawableRes int icon, @StringRes int label, int count) {
        View t = root.findViewById(includeId);
        ((ImageView) t.findViewById(R.id.admTopicIcon)).setImageResource(icon);
        ((TextView) t.findViewById(R.id.admTopicLabel)).setText(label);
        ((TextView) t.findViewById(R.id.admTopicCount)).setText(getString(R.string.adm_help_articles, count));
    }

    private void buildFaqs(View root) {
        final LinearLayout container = root.findViewById(R.id.admFaqContainer);
        final int[][] qa = {
                {R.string.adm_help_q1, R.string.adm_help_a1},
                {R.string.adm_help_q2, R.string.adm_help_a2},
                {R.string.adm_help_q3, R.string.adm_help_a3},
                {R.string.adm_help_q4, R.string.adm_help_a4},
        };
        final LayoutInflater inf = LayoutInflater.from(this);
        final List<View> answers = new ArrayList<>();
        final List<ImageView> chevrons = new ArrayList<>();
        for (int i = 0; i < qa.length; i++) {
            View item = inf.inflate(R.layout.item_admin_faq, container, false);
            ((TextView) item.findViewById(R.id.admFaqQ)).setText(qa[i][0]);
            ((TextView) item.findViewById(R.id.admFaqA)).setText(qa[i][1]);
            answers.add(item.findViewById(R.id.admFaqA));
            chevrons.add(item.findViewById(R.id.admFaqChevron));
            if (i == qa.length - 1) item.findViewById(R.id.admFaqDivider).setVisibility(View.GONE);
            final int idx = i;
            item.findViewById(R.id.admFaqHeader)
                    .setOnClickListener(v -> toggleFaq(idx, answers, chevrons));
            container.addView(item);
        }
        // First question expanded by default (matches the prototype).
        toggleFaq(0, answers, chevrons);
    }

    private void toggleFaq(int idx, List<View> answers, List<ImageView> chevrons) {
        if (openFaq == idx) {
            collapseFaq(idx, answers, chevrons);
            openFaq = -1;
            return;
        }
        if (openFaq != -1) collapseFaq(openFaq, answers, chevrons);
        answers.get(idx).setVisibility(View.VISIBLE);
        chevrons.get(idx).animate().rotation(180f).setDuration(180).start();
        openFaq = idx;
    }

    private void collapseFaq(int idx, List<View> answers, List<ImageView> chevrons) {
        answers.get(idx).setVisibility(View.GONE);
        chevrons.get(idx).animate().rotation(0f).setDuration(180).start();
    }

    private void contactSupport() {
        Intent email = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@tourgo.app"));
        email.putExtra(Intent.EXTRA_SUBJECT, "TourGo admin support");
        try {
            startActivity(email);
        } catch (ActivityNotFoundException e) {
            toast(R.string.adm_toast_no_mail);
        }
    }

    // ── Shared helpers ──────────────────────────────────────────────────────────
    private TextView groupLabel(CharSequence text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setAllCaps(true);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(color(R.color.adm_gray_400));
        t.setTextSize(12f);
        t.setLetterSpacing(0.04f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(14);
        lp.bottomMargin = dp(8);
        t.setLayoutParams(lp);
        return t;
    }

    private MaterialCardView newCard() {
        MaterialCardView c = new MaterialCardView(this);
        c.setRadius(dp(20));
        c.setCardElevation(dp(3));
        c.setCardBackgroundColor(color(R.color.white));
        c.setStrokeWidth(0);
        c.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return c;
    }

    private void addDivider(LinearLayout parent) {
        View d = new View(this);
        d.setBackgroundColor(color(R.color.adm_gray_100));
        d.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
        parent.addView(d);
    }

    private int dp(float v) {
        return AdminUi.dp(this, v);
    }

    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }

    private void toast(@StringRes int res) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
    }

    /** Toast a raw server/error message, falling back to a generic line. */
    private void toastMsg(String message) {
        Toast.makeText(this,
                message != null && !message.isEmpty() ? message : getString(R.string.adm_error_generic),
                Toast.LENGTH_SHORT).show();
    }
}
