import "pe"
import "math"

/*
 * Example YARA rules for testing syntax highlighting.
 * This demonstrates various YARA language constructs.
 */

// A simple rule to detect suspicious strings
rule SuspiciousExecutable : malware {
    meta:
        author = "Security Researcher"
        description = "Detects suspicious executable patterns"
        date = "2024-01-15"
        severity = 8
        is_malware = true

    strings:
        $mz_header = "MZ"
        $suspicious_api1 = "VirtualAlloc" nocase wide
        $suspicious_api2 = "WriteProcessMemory" fullword
        $hex_pattern = { E8 ?? ?? ?? ?? 83 C4 04 85 C0 }
        $regex_pattern = /https?:\/\/[a-zA-Z0-9\-\.]+\.[a-zA-Z]{2,}/
        $base64_string = "SGVsbG8gV29ybGQ=" base64

    condition:
        $mz_header at 0 and
        (any of ($suspicious_api*)) and
        (#hex_pattern > 2 or @regex_pattern[0] < 0x1000) and
        filesize < 5MB
}

private rule InternalHelper {
    strings:
        $a = "helper_string"

    condition:
        $a
}

global rule AlwaysCheck {
    condition:
        filesize < 10MB
}

rule CombinedRule {
    condition:
        SuspiciousExecutable and
        not InternalHelper and
        for all of them : ($ at entrypoint)
}
