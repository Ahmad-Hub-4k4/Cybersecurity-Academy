package com.example.data

data class QuizQuestionItem(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class CurriculumResourceSpec(
    val resourceId: String,
    val title: String,
    val url: String,
    val alternativeTitle: String,
    val alternativeUrl: String,
    val resourceType: String, // "Official Documentation", "Free Lab Guide", "RFC / Standard"
    val verificationStatus: String // "Verified Official URL"
)

data class CurriculumDayLesson(
    val dayNumber: Int, // 1..182
    val weekNumber: Int, // 1..26
    val dayOfWeekInCurriculum: Int, // 1..7
    val phaseNumber: Int, // 1..7
    val phaseTitle: String,
    val weekTheme: String,
    val title: String,
    val isWeeklyReviewDay: Boolean,
    val estimatedMinutes: Int, // 90 min standard, 60 min review day
    val timeAllocationPlan: String,
    val objectives: List<String>,
    val theorySummary: String,
    val commandsAndTerminology: List<String>,
    val guidedExercise: String,
    val independentChallenge: String,
    val expectedResultsAndTroubleshooting: String,
    val safeLabNotice: String,
    val revisionChecklist: List<String>,
    val practicalAssessmentPrompt: String,
    val completionCriteria: String,
    val primaryResource: CurriculumResourceSpec,
    val youtubeSearchQuery: String,
    val tutoringPrompt: String,
    val quizQuestions: List<QuizQuestionItem>
)

data class FlashcardItem(
    val id: String,
    val phaseNumber: Int,
    val weekNumber: Int,
    val category: String,
    val question: String,
    val answer: String
)

data class PhaseSpec(
    val phaseNumber: Int,
    val title: String,
    val weeks: IntRange,
    val focusSummary: String
)

object CurriculumCatalog {

    val phases: List<PhaseSpec> = listOf(
        PhaseSpec(
            phaseNumber = 1,
            title = "Phase 1: Linux Foundations",
            weeks = 1..4,
            focusSummary = "Terminal navigation, files, permissions, users, processes, package management, logs, Bash fundamentals, and basic system auditing."
        ),
        PhaseSpec(
            phaseNumber = 2,
            title = "Phase 2: Networking Fundamentals",
            weeks = 5..8,
            focusSummary = "TCP/IP, OSI, IPv4, subnetting, DNS, DHCP, ARP, TCP/UDP, routing, common ports, Wireshark, and network troubleshooting."
        ),
        PhaseSpec(
            phaseNumber = 3,
            title = "Phase 3: Security Foundations",
            weeks = 9..12,
            focusSummary = "Security principles, threat modelling, authentication, access control, hashing, encryption concepts, system hardening, log analysis, and incident response."
        ),
        PhaseSpec(
            phaseNumber = 4,
            title = "Phase 4: Security Testing Foundations",
            weeks = 13..16,
            focusSummary = "OSINT fundamentals, reconnaissance, Nmap, service enumeration, HTTP, web application fundamentals, Burp Suite, and authorised vulnerability assessment."
        ),
        PhaseSpec(
            phaseNumber = 5,
            title = "Phase 5: Defensive Security and SOC",
            weeks = 17..19,
            focusSummary = "Log analysis, alert triage, threat intelligence, incident investigation, detection logic, security monitoring, and introductory SIEM concepts."
        ),
        PhaseSpec(
            phaseNumber = 6,
            title = "Phase 6: Cloud Security and Automation",
            weeks = 20..22,
            focusSummary = "Cloud fundamentals, IAM, least privilege, storage security, networking controls, Python fundamentals, Bash automation, and security scripting."
        ),
        PhaseSpec(
            phaseNumber = 7,
            title = "Phase 7: Portfolio and Capstone",
            weeks = 23..26,
            focusSummary = "Combining previous skills, completing documented practical labs, building security automation tools, writing incident reports, improving GitHub repositories, and preparing a final capstone demonstration."
        )
    )

    private val weekSpecifications: Map<Int, Triple<Int, String, List<String>>> = mapOf(
        // Phase 1: Weeks 1-4
        1 to Triple(1, "Terminal Navigation & File Operations", listOf(
            "Linux CLI Orientation & Man Pages",
            "Directory Navigation (pwd, ls, cd, mkdir)",
            "Safe File Operations (cp, mv, rm -i, touch)",
            "File Permissions & Ownership (ls -l, chmod, chown)",
            "Viewing & Filtering Text (cat, less, head, tail, grep)",
            "Finding Files & Streams (find, locate, pipes, redirection)",
            "Week 1 Lab Review & Linux CLI Consolidation"
        )),
        2 to Triple(1, "Users, Groups, Processes & Services", listOf(
            "Linux User & Group Administration (/etc/passwd, /etc/group)",
            "Privilege Escalation Boundaries & sudoers Security",
            "Process Inspection & Signals (ps, top, kill, nice)",
            "Systemd Services & Daemons (systemctl, journalctl)",
            "Package Management in Debian/Kali (apt, dpkg)",
            "Disk Usage, Mounts & Inode Inspection (df, du, lsblk)",
            "Week 2 Lab Review & Process Auditing Challenge"
        )),
        3 to Triple(1, "System Logs, Cron & Bash Fundamentals", listOf(
            "Linux Log Architecture (/var/log/auth.log, syslog)",
            "Scheduled Tasks & Persistence Checks (cron, systemd timers)",
            "Bash Variables, Environment & Quoting Rules",
            "Bash Conditionals, Loops & Exit Codes",
            "Writing Safe Log-Parsing Shell Scripts",
            "SSH Client Configuration & Key-Based Authentication",
            "Week 3 Lab Review & Automated Log Parser Mini-Lab"
        )),
        4 to Triple(1, "Basic Linux System Auditing & Hardening", listOf(
            "Auditing SUID/SGID Binaries & World-Writable Files",
            "Inspecting Open Listening Sockets (ss -tulnp, lsof)",
            "Host Firewall Basics with UFW & iptables Concepts",
            "File Integrity Verification with SHA-256 Checksums",
            "Linux Baseline Hardening Checklist in Kali/Ubuntu VM",
            "Documenting a Host Security Audit Report",
            "Week 4 Phase 1 Milestone Review & Practical Assessment"
        )),
        // Phase 2: Weeks 5-8
        5 to Triple(2, "OSI Model, TCP/IP & IPv4 Addressing", listOf(
            "OSI 7-Layer vs TCP/IP 4-Layer Encapsulation",
            "IPv4 Addressing, Netmasks & CIDR Notation",
            "Practical Subnetting & Host Range Calculations",
            "Private vs Public IPv4 Space (RFC 1918) & NAT",
            "Layer 2 Ethernet Frames, MAC Addresses & ARP Table Inspection",
            "Inspecting Interfaces & Routes on Linux (ip addr, ip route)",
            "Week 5 Review & Subnetting Speed Drills"
        )),
        6 to Triple(2, "Core Network Protocols: TCP, UDP, DNS & DHCP", listOf(
            "TCP Three-Way Handshake, Flags (SYN, ACK, FIN, RST) & State",
            "UDP Characteristics & Stateless Transport Trade-offs",
            "Common Well-Known Ports & Service Identification",
            "DNS Hierarchy, Record Types (A, AAAA, MX, TXT, NS) & dig",
            "DHCP DORA Process & Lease Inspection",
            "ICMP Diagnostics (ping, traceroute, tracepath)",
            "Week 6 Review & Protocol Header Analysis"
        )),
        7 to Triple(2, "Packet Capture & Wireshark Fundamentals", listOf(
            "Setting Up Safe Packet Captures in Local VirtualBox NatNetwork",
            "Wireshark Capture vs Display Filters",
            "Following TCP & HTTP Streams in Wireshark",
            "Analyzing ARP, DNS & ICMP Traffic in PCAPs",
            "Capturing & Filtering Traffic with tcpdump on Kali CLI",
            "Detecting Abnormal Connection Resets & Retransmissions",
            "Week 7 Review & PCAP Investigation Lab"
        )),
        8 to Triple(2, "Routing, Network Troubleshooting & Baseline Traffic", listOf(
            "Static Routing & Default Gateway Diagnostics",
            "Troubleshooting Connectivity Layer by Layer (L1 to L7)",
            "TLS/HTTPS Handshake Inspection in Wireshark",
            "Establishing a Local Network Traffic Baseline",
            "Identifying Unauthorized Cleartext Protocols (Telnet, FTP)",
            "Writing a Network Topology & Packet Analysis Report",
            "Week 8 Phase 2 Milestone Review & Networking Assessment"
        )),
        // Phase 3: Weeks 9-12
        9 to Triple(3, "Security Principles, CIA Triad & Threat Modelling", listOf(
            "Confidentiality, Integrity, Availability & Defense-in-Depth",
            "Least Privilege, Separation of Duties & Zero Trust Concepts",
            "Threat Modelling with STRIDE & Attack Trees",
            "Assets, Threat Actors, Vulnerabilities, Risk & Mitigations",
            "CVE, CVSS Scoring & NVD Vulnerability Research",
            "Security Controls: Preventive, Detective & Corrective",
            "Week 9 Review & Local Lab Threat Model Exercise"
        )),
        10 to Triple(3, "Authentication, Access Control & Cryptography", listOf(
            "Authentication Factors (MFA), Passwords & Salted Key Derivation",
            "Access Control Models: DAC, MAC, RBAC & ABAC",
            "Cryptographic Hashing (SHA-256) & Integrity Verification",
            "Symmetric vs Asymmetric Encryption (AES, RSA, ECDH)",
            "Public Key Infrastructure (PKI), X.509 Certificates & TLS",
            "Practical OpenSSL CLI Hashing & Certificate Inspection",
            "Week 10 Review & Applied Crypto Verification Lab"
        )),
        11 to Triple(3, "System Hardening & Endpoint Security Controls", listOf(
            "CIS Benchmark Concepts & Attack Surface Reduction",
            "Securing SSH Daemon (/etc/ssh/sshd_config) Against Brute Force",
            "Linux Account Policies, PAM & Password Aging",
            "Application Sandboxing & Mandatory Access Control (AppArmor)",
            "Unattended Security Updates & Patch Verification",
            "Auditing Local Host Security Posture with Lynis",
            "Week 11 Review & VM Hardening Verification"
        )),
        12 to Triple(3, "Log Analysis & Incident Response Foundations", listOf(
            "Incident Response Lifecycle (NIST SP 800-61 Phases)",
            "Analyzing Linux Authentication Logs for Failed Logins",
            "Correlating Network Connections with Local Process Activity",
            "Evidence Preservation, Timestamps (UTC) & Chain of Custody",
            "Drafting an Initial Incident Triage & Containment Note",
            "Tabletop Scenario: Unauthorized SSH Key Addition",
            "Week 12 Phase 3 Milestone Review & Security Foundations Exam"
        )),
        // Phase 4: Weeks 13-16 (Completing & Expanding Roadmap)
        13 to Triple(4, "Authorised Reconnaissance & OSINT Fundamentals", listOf(
            "Rules of Engagement, Legal Authorisation & Lab Scope Boundaries",
            "Passive OSINT Principles & Public Metadata Hygiene",
            "DNS Enumeration & Subdomain Discovery in Lab Environments",
            "WHOIS, Certificate Transparency Logs & Asset Inventory",
            "Organizing Reconnaissance Findings in Structured Notes",
            "Defensive Counter-Reconnaissance: Reducing Public Exposure",
            "Week 13 Review & Authorised Recon Documentation"
        )),
        14 to Triple(4, "Network Discovery & Nmap Service Enumeration", listOf(
            "Host Discovery Techniques in Isolated VirtualBox Lab Networks",
            "Nmap Port Scanning Mechanics (-sS, -sT, -sU) & Scan States",
            "Service & Version Detection (-sV) and OS Fingerprinting",
            "Nmap Scripting Engine (NSE) Safe Discovery Scripts",
            "Saving & Comparing Nmap Outputs (-oN, -oX, -oG)",
            "How Defenders Detect Port Scans in Firewall & Zeek Logs",
            "Week 14 Review & Local Lab Enumeration Report"
        )),
        15 to Triple(4, "HTTP Protocol & Web Application Fundamentals", listOf(
            "HTTP Request/Response Structure, Methods, Headers & Status Codes",
            "Cookies, Sessions, Tokens & Same-Origin Policy",
            "Inspecting Web Traffic with Browser DevTools & curl",
            "OWASP Top 10 Overview: Broken Access Control & Injection",
            "Cross-Site Scripting (XSS) & CSRF Mechanics and Defenses",
            "Security Headers (CSP, HSTS, X-Frame-Options) Inspection",
            "Week 15 Review & Local Web App Header Audit"
        )),
        16 to Triple(4, "Burp Suite Proxy & Authorised Vulnerability Assessment", listOf(
            "Configuring Burp Suite Community Proxy with Local Lab Browser",
            "Intercepting, Inspecting & Repeating HTTP Requests Safely",
            "Input Validation Testing & Parameter Tampering in Local Labs",
            "Authentication & Session Management Flaws in Practice Apps",
            "Validating Findings & Eliminating False Positives",
            "Writing Remedial Recommendations (Parameterized Queries, Output Encoding)",
            "Week 16 Phase 4 Milestone Review & Web Assessment Write-up"
        )),
        // Phase 5: Weeks 17-19
        17 to Triple(5, "Defensive Security, SOC Workflows & Alert Triage", listOf(
            "SOC Tier 1-3 Responsibilities & Alert Triage Methodology",
            "Understanding True Positives, False Positives & Benign Triggers",
            "Analyzing Web Server Access & Error Logs (Apache/Nginx)",
            "Detecting Brute-Force, Directory Traversal & SQLi in HTTP Logs",
            "Windows Event Log Essentials (Event IDs 4624, 4625, 4688)",
            "Building a Structured Alert Triage Playbook",
            "Week 17 Review & Log Triage Case Study"
        )),
        18 to Triple(5, "Threat Intelligence, MITRE ATT&CK & Incident Investigation", listOf(
            "MITRE ATT&CK Framework: Tactics, Techniques & Procedures (TTPs)",
            "Pyramid of Pain & Indicators of Compromise (Hashes, IPs, Domains)",
            "Investigating Suspicious Persistence & Scheduled Tasks",
            "Analyzing Phishing Headers & Safe URL/Attachment Inspection",
            "Timeline Reconstruction Across Host & Network Logs",
            "Containment, Eradication & Recovery Decision Making",
            "Week 18 Review & Incident Timeline Reconstruction Lab"
        )),
        19 to Triple(5, "Detection Logic, Sigma Rules & Introductory SIEM Concepts", listOf(
            "SIEM Architecture: Log Collection, Normalization & Correlation",
            "Writing Structured Search Queries for Security Events",
            "Introduction to Sigma Detection Rules & YAML Syntax",
            "YARA Rules Concepts for File Pattern Identification",
            "Tuning Detection Rules to Reduce Alert Fatigue",
            "Drafting a Complete SOC Incident Investigation Report",
            "Week 19 Phase 5 Milestone Review & SOC Analyst Assessment"
        )),
        // Phase 6: Weeks 20-22
        20 to Triple(6, "Cloud Security Fundamentals, IAM & Least Privilege", listOf(
            "Cloud Shared Responsibility Model (IaaS, PaaS, SaaS)",
            "Identity and Access Management (IAM) Users, Roles & Policies",
            "Evaluating IAM JSON Policies for Least Privilege Violations",
            "Multi-Factor Authentication & Root/Admin Account Protection",
            "Cloud Audit Logging & Control Plane Monitoring Concepts",
            "Preventing Credential Leakage in Git Repositories (.gitignore, pre-commit)",
            "Week 20 Review & IAM Policy Audit Exercise"
        )),
        21 to Triple(6, "Cloud Storage, Networking Controls & Container Basics", listOf(
            "Object Storage Security: Public Access Blocks & Bucket Policies",
            "Cloud Virtual Networks (VPC), Subnets, Security Groups & NACLs",
            "Encryption at Rest (KMS) and Encryption in Transit",
            "Container Security Basics: Dockerfiles, Non-Root Users & Base Images",
            "Cloud Posture Management (CSPM) & Misconfiguration Checks",
            "Reviewing a Cloud Architecture Diagram for Security Gaps",
            "Week 21 Review & Cloud Security Hardening Checklist"
        )),
        22 to Triple(6, "Python & Bash Security Automation Scripting", listOf(
            "Python Environment Setup (venv) & Safe Scripting Practices",
            "Parsing JSON & CSV Security Logs with Python",
            "Automating SHA-256 File Integrity Baselines in Python",
            "Writing a Python Port & Banner Verification Tool for Local Labs",
            "Automating Linux Security Baseline Checks with Bash",
            "Testing, Documenting & Formatting Code for GitHub",
            "Week 22 Phase 6 Milestone Review & Security Automation Lab"
        )),
        // Phase 7: Weeks 23-26
        23 to Triple(7, "End-to-End Lab Integration: Attack & Defense Correlation", listOf(
            "Designing an Isolated Dual-VM Lab (Kali + Target/Monitor VM)",
            "Executing Authorised Enumeration & Capturing Defender Logs",
            "Correlating Nmap & Web Requests with Target System Logs",
            "Applying Host Hardening & Verifying Mitigation Effectiveness",
            "Validating Firewall & SSH Controls Against Repeat Tests",
            "Documenting Before-and-After Security Posture Evidence",
            "Week 23 Review & Purple-Team Lab Documentation"
        )),
        24 to Triple(7, "Building a Security Automation Tool for GitHub Portfolio", listOf(
            "Scoping a Practical CLI Security Tool (Log Analyzer / Baseline Auditor)",
            "Implementing Modular Core Logic & CLI Arguments (argparse)",
            "Adding Input Validation, Error Handling & Sample Test Data",
            "Writing Unit Tests for Log Parsing & Detection Rules",
            "Creating a Professional README with Architecture & Usage Examples",
            "Publishing Clean Commits & Release Tags for Portfolio Review",
            "Week 24 Review & Portfolio Code Quality Audit"
        )),
        25 to Triple(7, "Professional Security Reporting & Documentation", listOf(
            "Structuring an Executive Summary vs Technical Findings Section",
            "Writing Reproducible Steps, Impact Analysis & CVSS Context",
            "Drafting Clear Remediation Guidance for Engineering Teams",
            "Compiling a SOC Incident Investigation Portfolio Case Study",
            "Organizing Lab Write-ups & Redacting Sensitive Environment Data",
            "Peer/Self-Review of Technical Writing Clarity and Accuracy",
            "Week 25 Review & Portfolio Documentation Polish"
        )),
        26 to Triple(7, "Final Capstone Demonstration & Career Readiness", listOf(
            "Capstone Day 1: Environment Verification & Scope Definition",
            "Capstone Day 2: Authorised Assessment & Evidence Collection",
            "Capstone Day 3: Log Investigation, Detection & Root-Cause Analysis",
            "Capstone Day 4: System Hardening & Automated Verification Script",
            "Capstone Day 5: Final Capstone Technical Report & Executive Summary",
            "Capstone Day 6: GitHub Portfolio Polish & Walkthrough Preparation",
            "Day 182 Graduation Review: 26-Week Foundations Retrospective"
        ))
    )

    private val cachedLessons: List<CurriculumDayLesson> by lazy {
        buildList(182) {
            for (week in 1..26) {
                val (phaseNum, weekTheme, dayTitles) = weekSpecifications[week]!!
                val phaseSpec = phases.first { it.phaseNumber == phaseNum }
                for (dayInWeek in 1..7) {
                    val globalDay = (week - 1) * 7 + dayInWeek
                    val title = dayTitles[dayInWeek - 1]
                    val isReview = dayInWeek == 7
                    add(
                        createLessonForDay(
                            dayNumber = globalDay,
                            weekNumber = week,
                            dayOfWeek = dayInWeek,
                            phaseSpec = phaseSpec,
                            weekTheme = weekTheme,
                            title = title,
                            isReview = isReview
                        )
                    )
                }
            }
        }
    }

    fun getAllLessons(): List<CurriculumDayLesson> = cachedLessons

    fun getLessonsForPhase(phaseNumber: Int): List<CurriculumDayLesson> =
        cachedLessons.filter { it.phaseNumber == phaseNumber }

    fun getLessonsForWeek(weekNumber: Int): List<CurriculumDayLesson> =
        cachedLessons.filter { it.weekNumber == weekNumber }

    fun getLessonByDay(dayNumber: Int): CurriculumDayLesson =
        cachedLessons.getOrElse((dayNumber - 1).coerceIn(0, 181)) { cachedLessons.first() }

    private fun createLessonForDay(
        dayNumber: Int,
        weekNumber: Int,
        dayOfWeek: Int,
        phaseSpec: PhaseSpec,
        weekTheme: String,
        title: String,
        isReview: Boolean
    ): CurriculumDayLesson {
        val resourceSpec = resourceForPhase(phaseSpec.phaseNumber, dayNumber, title)
        val commands = commandsForPhase(phaseSpec.phaseNumber, weekNumber, dayOfWeek)
        val estMins = if (isReview) 60 else 90
        val plan = if (isReview) {
            "15m Weekly Concept Review • 25m Consolidation Lab • 15m Self-Quiz & Weak-Area Check • 5m Weekly Summary Log"
        } else {
            "10m Review • 25m Learn • 35m Hands-on Practice • 15m Independent Challenge • 5m Record Results"
        }

        return CurriculumDayLesson(
            dayNumber = dayNumber,
            weekNumber = weekNumber,
            dayOfWeekInCurriculum = dayOfWeek,
            phaseNumber = phaseSpec.phaseNumber,
            phaseTitle = phaseSpec.title,
            weekTheme = weekTheme,
            title = title,
            isWeeklyReviewDay = isReview,
            estimatedMinutes = estMins,
            timeAllocationPlan = plan,
            objectives = listOf(
                "Understand the core principles of $title within ${phaseSpec.title}.",
                "Execute safe, hands-on verification in your isolated Kali Linux VirtualBox lab.",
                "Document terminal outputs, defensive takeaways, and troubleshooting notes."
            ),
            theorySummary = "Day $dayNumber focuses on $title ($weekTheme). ${phaseSpec.focusSummary} " +
                "Focus on how each command or protocol behaves under normal conditions so you can reliably spot misconfigurations or security anomalies.",
            commandsAndTerminology = commands,
            guidedExercise = "1. Boot your isolated Kali Linux VM in VirtualBox.\n" +
                "2. Create or navigate to ~/cyber_academy/week_$weekNumber/day_$dayNumber.\n" +
                "3. Run and inspect the reference commands (${commands.take(2).joinToString(", ")}) and record their flags and output structure.",
            independentChallenge = "Without looking at command hints, reproduce today's workflow on a sample test file or local interface in ~/cyber_academy/week_$weekNumber, verify the result, and explain how a defender or administrator would audit this activity.",
            expectedResultsAndTroubleshooting = "Expected: Clean command execution with zero unintended file loss and a documented lab note. Troubleshooting: If permission is denied or a flag fails, check 'man <command>' or '--help' and verify your current working directory and user privileges.",
            safeLabNotice = "SAFE LAB RULE: Perform all exercises strictly inside your own authorised Kali Linux VirtualBox VM, local loopback/NatNetwork lab, or sample log files. Never scan or test external/public systems without explicit written authorisation.",
            revisionChecklist = listOf(
                "Reviewed key terminology and flags for $title",
                "Completed 35-minute hands-on terminal/lab exercise in Kali VM",
                "Completed 15-minute independent challenge without hints",
                "Answered the 5 daily verification quiz questions"
            ),
            practicalAssessmentPrompt = "Capture the key command output or configuration snippet from your Kali VM demonstrating $title and summarize its security significance in 2–3 sentences.",
            completionCriteria = "Complete the guided exercise, independent challenge, 5-question quiz, and record your lab notes.",
            primaryResource = resourceSpec,
            youtubeSearchQuery = "Kali Linux cybersecurity tutorial $title beginner foundations",
            tutoringPrompt = "Act as a cybersecurity lab tutor for a 5th-semester BS Computer Engineering student using Kali Linux in VirtualBox. Explain '$title' step by step, quiz me with 3 practical scenario questions, and explain both the offensive/testing perspective and the defensive SOC perspective.",
            quizQuestions = buildQuizQuestionsForDay(dayNumber, weekNumber, phaseSpec.phaseNumber, title, commands)
        )
    }

    private fun commandsForPhase(phase: Int, week: Int, dayOfWeek: Int): List<String> {
        return when (phase) {
            1 -> listOf(
                "pwd, ls -la, cd, mkdir -p ~/cyber_lab",
                "cp -i, mv -i, rm -i, chmod 750, chown",
                "grep -rn, find . -perm /4000, ps aux, ss -tulnp",
                "journalctl -xe, tail -f /var/log/auth.log, sha256sum"
            )
            2 -> listOf(
                "ip addr show, ip route show, ip neigh",
                "ping -c 4, traceroute, dig +short, nslookup",
                "ss -tulnp, tcpdump -i any -nn port 53",
                "Wireshark display filters: tcp.flags.syn==1, http, dns"
            )
            3 -> listOf(
                "sha256sum, openssl dgst -sha256, openssl x509 -in cert.pem -text -noout",
                "sudo ufw status verbose, sudo sshd -T",
                "grep 'Failed password' /var/log/auth.log | awk '{print $11}' | sort | uniq -c",
                "lynis audit system --quick (local VM baseline check)"
            )
            4 -> listOf(
                "nmap -sn 10.0.2.0/24 (isolated VirtualBox NatNetwork only)",
                "nmap -sV -sC -oN scan_report.txt <local_lab_ip>",
                "curl -I http://127.0.0.1:8080, curl -v",
                "Burp Suite Proxy: 127.0.0.1:8080 Intercept & Repeater"
            )
            5 -> listOf(
                "awk '{print $1, $7, $9}' access.log | sort | uniq -c | sort -nr",
                "grep -E 'UNION|SELECT|../|<script>' access.log",
                "Windows Security Event IDs: 4624 (Logon), 4625 (Failed Logon), 4688 (Process)",
                "Sigma rule structure: title, logsource, detection, condition, level"
            )
            6 -> listOf(
                "IAM Policy elements: Effect, Action, Resource, Condition",
                "python3 -m venv .venv && source .venv/bin/activate",
                "python3 hashlib.sha256(), json.loads(), csv.DictReader",
                "git status, .gitignore secret hygiene, chmod 600 ~/.ssh/id_ed25519"
            )
            else -> listOf(
                "python3 -m unittest discover -s tests",
                "git log --oneline --graph, git tag -a v1.0.0",
                "Report sections: Executive Summary, Scope, Findings, Evidence, Remediation",
                "Capstone verification: baseline audit -> log correlation -> remediation test"
            )
        }.rotateBy((week + dayOfWeek) % 4)
    }

    private fun <T> List<T>.rotateBy(offset: Int): List<T> {
        if (isEmpty()) return this
        val shift = offset.mod(size)
        return drop(shift) + take(shift)
    }

    private fun resourceForPhase(phase: Int, dayNumber: Int, title: String): CurriculumResourceSpec {
        return when (phase) {
            1 -> CurriculumResourceSpec(
                resourceId = "res_day_$dayNumber",
                title = "Debian & GNU Coreutils Manual: $title",
                url = "https://www.debian.org/doc/manuals/debian-reference/",
                alternativeTitle = "Linux Journey — Command Line & Permissions",
                alternativeUrl = "https://linuxjourney.com/",
                resourceType = "Official Documentation",
                verificationStatus = "Verified Official URL"
            )
            2 -> CurriculumResourceSpec(
                resourceId = "res_day_$dayNumber",
                title = "Wireshark Official User's Guide: $title",
                url = "https://www.wireshark.org/docs/wsug_html_chunked/",
                alternativeTitle = "IETF RFCs & Linux iproute2 Documentation",
                alternativeUrl = "https://man7.org/linux/man-pages/man8/ip.8.html",
                resourceType = "Official Documentation",
                verificationStatus = "Verified Official URL"
            )
            3 -> CurriculumResourceSpec(
                resourceId = "res_day_$dayNumber",
                title = "NIST Cybersecurity & Incident Handling Guides: $title",
                url = "https://csrc.nist.gov/publications/sp800",
                alternativeTitle = "OWASP Cheat Sheet Series — Defense & Hardening",
                alternativeUrl = "https://cheatsheetseries.owasp.org/",
                resourceType = "Official Standard / Guide",
                verificationStatus = "Verified Official URL"
            )
            4 -> CurriculumResourceSpec(
                resourceId = "res_day_$dayNumber",
                title = "OWASP Web Security Testing Guide (WSTG) & Nmap Reference: $title",
                url = "https://owasp.org/www-project-web-security-testing-guide/",
                alternativeTitle = "Nmap Official Reference Guide",
                alternativeUrl = "https://nmap.org/book/man.html",
                resourceType = "Official Documentation",
                verificationStatus = "Verified Official URL"
            )
            5 -> CurriculumResourceSpec(
                resourceId = "res_day_$dayNumber",
                title = "MITRE ATT&CK Matrix & Sigma Detection Specification: $title",
                url = "https://attack.mitre.org/",
                alternativeTitle = "SigmaHQ Rule Specification Documentation",
                alternativeUrl = "https://sigmahq.io/",
                resourceType = "Official Framework",
                verificationStatus = "Verified Official URL"
            )
            6 -> CurriculumResourceSpec(
                resourceId = "res_day_$dayNumber",
                title = "OWASP Cloud-Native Security & Python Standard Library Docs: $title",
                url = "https://docs.python.org/3/library/",
                alternativeTitle = "CIS Cloud & Linux Benchmark Overview",
                alternativeUrl = "https://www.cisecurity.org/cis-benchmarks",
                resourceType = "Official Documentation",
                verificationStatus = "Verified Official URL"
            )
            else -> CurriculumResourceSpec(
                resourceId = "res_day_$dayNumber",
                title = "GitHub Documentation & OWASP Reporting Best Practices: $title",
                url = "https://docs.github.com/en",
                alternativeTitle = "CISA Cybersecurity Advisories & Reporting Templates",
                alternativeUrl = "https://www.cisa.gov/cybersecurity-training-exercises",
                resourceType = "Official Documentation",
                verificationStatus = "Verified Official URL"
            )
        }
    }

    private fun buildQuizQuestionsForDay(
        dayNumber: Int,
        weekNumber: Int,
        phaseNumber: Int,
        title: String,
        commands: List<String>
    ): List<QuizQuestionItem> {
        val primaryCmd = commands.firstOrNull() ?: "man"
        return listOf(
            QuizQuestionItem(
                id = 1,
                question = "Day $dayNumber ($title): Why must all security commands and scans be executed strictly in an authorised lab environment?",
                options = listOf(
                    "To comply with legal/ethical boundaries and prevent unintended impact on external systems",
                    "Because Linux commands only run inside VirtualBox",
                    "To skip logging terminal results"
                ),
                correctIndex = 0,
                explanation = "Testing without explicit permission is illegal and unethical; isolated VirtualBox VMs allow safe experimentation."
            ),
            QuizQuestionItem(
                id = 2,
                question = "Which command group is most relevant to practicing today's topic ($title)?",
                options = listOf(
                    primaryCmd,
                    "format C: /fs:NTFS",
                    "drop database production"
                ),
                correctIndex = 0,
                explanation = "'$primaryCmd' directly supports inspecting and verifying $title in Week $weekNumber."
            ),
            QuizQuestionItem(
                id = 3,
                question = "In the 90-minute daily study structure, how much time is dedicated to hands-on lab practice?",
                options = listOf(
                    "5 minutes",
                    "35 minutes (plus 15 minutes independent challenge)",
                    "90 minutes of passive video watching only"
                ),
                correctIndex = 1,
                explanation = "The 90-minute plan allocates 10m Review, 25m Learn, 35m Hands-on Practice, 15m Independent Challenge, and 5m Record Results."
            ),
            QuizQuestionItem(
                id = 4,
                question = "From a defensive (SOC / system administrator) viewpoint, what is the primary benefit of understanding $title?",
                options = listOf(
                    "Establishing an accurate baseline to distinguish normal system behavior from anomalies",
                    "Disabling all system logs to save disk space",
                    "Granting root privileges to all guest users"
                ),
                correctIndex = 0,
                explanation = "Defenders rely on baseline familiarity with system, network, and application behavior to detect intrusions early."
            ),
            QuizQuestionItem(
                id = 5,
                question = "What is required before marking Day $dayNumber ($title) complete?",
                options = listOf(
                    "Only clicking a resource link without reading or practicing",
                    "Completing the hands-on exercise, independent challenge, quiz, and recording lab notes",
                    "Waiting until Week 26"
                ),
                correctIndex = 1,
                explanation = "Opening a resource link never auto-completes a lesson; practical verification and notes are required."
            )
        )
    }

    val flashcards: List<FlashcardItem> = listOf(
        FlashcardItem(
            id = "fc_linux_1",
            phaseNumber = 1,
            weekNumber = 1,
            category = "Linux Permissions",
            question = "What does 'chmod 750 deploy.sh' set for Owner, Group, and Others?",
            answer = "Owner: rwx (7 = 4+2+1), Group: r-x (5 = 4+0+1), Others: --- (0). Only owner can modify; owner and group can execute."
        ),
        FlashcardItem(
            id = "fc_linux_2",
            phaseNumber = 1,
            weekNumber = 2,
            category = "Linux Auditing",
            question = "Which command lists all currently listening TCP and UDP sockets along with their process IDs?",
            answer = "'ss -tulnp' (or 'sudo ss -tulnp' to view process names owned by root/system users)."
        ),
        FlashcardItem(
            id = "fc_net_1",
            phaseNumber = 2,
            weekNumber = 5,
            category = "Networking & Subnetting",
            question = "How many usable host IPv4 addresses exist in a /24 subnet (255.255.255.0)?",
            answer = "254 usable hosts (2^8 - 2 = 256 - 2, excluding the network address .0 and broadcast address .255)."
        ),
        FlashcardItem(
            id = "fc_net_2",
            phaseNumber = 2,
            weekNumber = 6,
            category = "TCP/IP Transport",
            question = "What are the three packets exchanged during a standard TCP 3-way handshake?",
            answer = "1. Client -> Server: SYN\n2. Server -> Client: SYN-ACK\n3. Client -> Server: ACK"
        ),
        FlashcardItem(
            id = "fc_sec_1",
            phaseNumber = 3,
            weekNumber = 9,
            category = "Threat Modelling",
            question = "What does the STRIDE mnemonic stand for in threat modelling?",
            answer = "Spoofing, Tampering, Repudiation, Information Disclosure, Denial of Service, Elevation of Privilege."
        ),
        FlashcardItem(
            id = "fc_sec_2",
            phaseNumber = 3,
            weekNumber = 10,
            category = "Cryptography",
            question = "Why should passwords be stored with a unique cryptographic salt and PBKDF2/Argon2 instead of a fast plain SHA-256 hash?",
            answer = "A unique random salt prevents rainbow-table attacks, and key-stretching (PBKDF2/Argon2) slows down offline brute-force cracking."
        ),
        FlashcardItem(
            id = "fc_pentest_1",
            phaseNumber = 4,
            weekNumber = 14,
            category = "Service Enumeration",
            question = "What is the purpose of the '-sV' flag in Nmap during an authorised lab scan?",
            answer = "Probes open ports to determine the running service protocol and software version."
        ),
        FlashcardItem(
            id = "fc_pentest_2",
            phaseNumber = 4,
            weekNumber = 15,
            category = "Web Security",
            question = "What is the primary defense against SQL Injection (SQLi) in web applications?",
            answer = "Parameterized queries (prepared statements) so user input is treated strictly as data, never executable SQL syntax."
        ),
        FlashcardItem(
            id = "fc_soc_1",
            phaseNumber = 5,
            weekNumber = 17,
            category = "SOC & Windows Logs",
            question = "In Windows Security Event Logs, what is the difference between Event ID 4624 and Event ID 4625?",
            answer = "Event ID 4624 records a successful account logon; Event ID 4625 records a failed account logon attempt."
        ),
        FlashcardItem(
            id = "fc_cloud_1",
            phaseNumber = 6,
            weekNumber = 20,
            category = "Cloud IAM",
            question = "Why is 'Action: \"*\", Resource: \"*\"' dangerous in an IAM policy?",
            answer = "It grants full wildcard administrative privileges across all cloud resources, violating the Principle of Least Privilege."
        ),
        FlashcardItem(
            id = "fc_capstone_1",
            phaseNumber = 7,
            weekNumber = 25,
            category = "Security Reporting",
            question = "What must every technical vulnerability or incident finding include to be actionable for engineers?",
            answer = "Clear scope/affected asset, reproducible evidence, risk/impact context, and specific remediation steps."
        )
    )

    fun getStarterProjects(): List<ProjectEntity> = listOf(
        ProjectEntity(
            id = 1,
            title = "Linux Security Baseline & SUID Auditor Script",
            difficulty = "Beginner (Phase 1)",
            status = "In Progress",
            objectives = "Write a safe Bash script in Kali Linux that audits world-writable files, SUID binaries, listening ports (ss -tulnp), and SSH configuration.",
            requiredTools = "Kali Linux VM, Bash, coreutils, ss, sha256sum",
            tasksMilestones = "1. Enumerate SUID/SGID files\n2. Check /etc/ssh/sshd_config PermitRootLogin\n3. Output timestamped Markdown summary report",
            notes = "Designed for local VM auditing without modifying system files.",
            evidenceLinks = "",
            githubUrl = "https://github.com/waqasahmad/linux-baseline-auditor",
            completionDate = "",
            weekAssociation = 4
        ),
        ProjectEntity(
            id = 2,
            title = "Local Lab PCAP & DNS/HTTP Traffic Analyzer",
            difficulty = "Intermediate (Phase 2 & 5)",
            status = "Planned",
            objectives = "Capture local VirtualBox NatNetwork traffic and document normal vs anomalous DNS and HTTP requests using Wireshark and tcpdump.",
            requiredTools = "Wireshark, tcpdump, Kali Linux VM, Python 3",
            tasksMilestones = "1. Capture baseline DNS & HTTP PCAP\n2. Apply display filters\n3. Write SOC packet analysis report",
            notes = "",
            evidenceLinks = "",
            githubUrl = "https://github.com/waqasahmad/pcap-traffic-analysis-lab",
            completionDate = "",
            weekAssociation = 8
        ),
        ProjectEntity(
            id = 3,
            title = "Python Auth Log Brute-Force Detector & Incident Report",
            difficulty = "Intermediate (Phase 5 & 6)",
            status = "Planned",
            objectives = "Build a Python CLI tool that parses sample /var/log/auth.log files, identifies brute-force IP thresholds, and generates a structured Markdown incident report.",
            requiredTools = "Python 3, Regular Expressions, Sample Linux auth.log, Git",
            tasksMilestones = "1. Parse failed SSH login timestamps and source IPs\n2. Flag IPs exceeding threshold\n3. Export JSON/Markdown alert report with unit tests",
            notes = "",
            evidenceLinks = "",
            githubUrl = "https://github.com/waqasahmad/soc-authlog-detector",
            completionDate = "",
            weekAssociation = 22
        ),
        ProjectEntity(
            id = 4,
            title = "Week 26 Capstone: End-to-End Lab Assessment, Hardening & SOC Report",
            difficulty = "Advanced Foundations (Phase 7)",
            status = "Planned",
            objectives = "Combine authorised enumeration, log detection, host hardening, and executive/technical reporting in an isolated dual-VM lab.",
            requiredTools = "Kali Linux VM, Target Lab VM, Nmap, Wireshark, UFW, Python/Bash",
            tasksMilestones = "1. Baseline Enumeration\n2. Log Correlation\n3. System Hardening\n4. Final Portfolio Report",
            notes = "",
            evidenceLinks = "",
            githubUrl = "https://github.com/waqasahmad/cybersecurity-capstone-v2",
            completionDate = "",
            weekAssociation = 26
        )
    )
}
