#!/usr/bin/env bash
# Generates safe demo fixtures and launches the IntelliJ sandbox on an
# isolated Xvfb display so Marketplace screenshots never touch a real desktop.
#
# Usage:
#   scripts/capture-marketplace-screenshots.sh --generate-only
#   scripts/capture-marketplace-screenshots.sh            # fixtures + runIde on :99
#
# Requires: JDK 21 (keytool), openssl, Xvfb, xdotool, ImageMagick.
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
demo_root="${CERTVIEW_DEMO_DIR:-/tmp/certview-demo}"
fixture_dir="$demo_root/fixtures"
display="${CERTVIEW_DISPLAY:-:99}"
password="changeit"

generate_fixtures() {
    rm -rf "$fixture_dir"
    mkdir -p "$fixture_dir"
    local work
    work="$(mktemp -d)"
    trap 'rm -rf "$work"' RETURN

    keytool -genkeypair -alias demo-root-ca -keyalg RSA -keysize 2048 \
        -validity 3650 -startdate -400d \
        -dname "CN=CertView Demo Root CA,O=CertView X.509 Demo,C=AR" -ext bc:c \
        -storetype PKCS12 -keystore "$work/ca.p12" -storepass "$password" -keypass "$password" -noprompt

    keytool -genkeypair -alias marketplace-demo -keyalg RSA -keysize 2048 -validity 365 \
        -dname "CN=api.demo.example,OU=Platform,O=CertView X.509 Demo,L=Buenos Aires,C=AR" \
        -storetype PKCS12 -keystore "$fixture_dir/demo-keystore.p12" \
        -storepass "$password" -keypass "$password" -noprompt

    keytool -certreq -alias marketplace-demo -keystore "$fixture_dir/demo-keystore.p12" \
        -storepass "$password" -file "$work/leaf.csr"

    keytool -gencert -alias demo-root-ca -keystore "$work/ca.p12" -storepass "$password" \
        -infile "$work/leaf.csr" -outfile "$work/leaf.crt" -rfc -startdate -120d -validity 365 \
        -ext "san=dns:api.demo.example,dns:www.demo.example" \
        -ext "ku=digitalSignature,keyEncipherment" -ext "eku=serverAuth"

    keytool -exportcert -rfc -alias demo-root-ca -keystore "$work/ca.p12" \
        -storepass "$password" -file "$work/ca.crt"

    keytool -importcert -alias demo-root-ca -file "$work/ca.crt" \
        -keystore "$fixture_dir/demo-keystore.p12" -storepass "$password" -noprompt

    cat "$work/leaf.crt" "$work/ca.crt" > "$work/chain.pem"
    keytool -importcert -alias marketplace-demo -file "$work/chain.pem" \
        -keystore "$fixture_dir/demo-keystore.p12" -storepass "$password" -noprompt

    cp "$work/leaf.crt" "$fixture_dir/demo-certificate.pem"
    openssl x509 -in "$work/leaf.crt" -outform DER -out "$fixture_dir/demo-certificate.der"

    keytool -importkeystore -srckeystore "$fixture_dir/demo-keystore.p12" -srcstoretype PKCS12 \
        -srcstorepass "$password" -destkeystore "$fixture_dir/demo-keystore.jks" \
        -deststoretype JKS -deststorepass "$password" -noprompt

    echo "Demo fixtures generated under $fixture_dir (password: $password)"
}

write_init_script() {
    # Skips first-run consent, trust and tips dialogs in the sandbox only.
    init_script="$(mktemp --suffix=.gradle.kts)"
    cat > "$init_script" <<'EOF'
allprojects {
    tasks.matching { it.name == "runIde" }.configureEach {
        (this as JavaExec).jvmArgs(
            "-Djb.consents.confirmation.enabled=false",
            "-Djb.privacy.policy.text=<!--999.999-->",
            "-Didea.trust.all.projects=true",
            "-Dide.show.tips.on.startup.default.value=false",
            "-Didea.initially.ask.config=never",
            "-Dide.newUsersOnboarding=false",
            "-Duser.language=en",
            "-Duser.country=US",
        )
    }
}
EOF
}

generate_fixtures
[[ "${1:-}" == "--generate-only" ]] && exit 0

write_init_script
if ! xdpyinfo -display "$display" >/dev/null 2>&1; then
    Xvfb "$display" -screen 0 1600x1000x24 -nolisten tcp >/dev/null 2>&1 &
    sleep 2
fi

echo "Launching sandbox IDE on $display. Capture with:"
echo "  DISPLAY=$display import -window <id> docs/assets/marketplace/<name>.png"
cd "$repo_root"
DISPLAY="$display" ./gradlew --init-script "$init_script" runIde \
    --args="$demo_root $fixture_dir/demo-certificate.pem"
