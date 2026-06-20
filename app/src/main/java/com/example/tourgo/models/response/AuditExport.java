package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

/** Payload of GET /api/admin/audit-log/export — the audit log rendered as CSV. */
public class AuditExport {
    @SerializedName("csv")
    private String csv;
    @SerializedName("filename")
    private String filename;
    @SerializedName("count")
    private int count;

    public String getCsv() { return csv; }
    public String getFilename() { return filename; }
    public int getCount() { return count; }
}
