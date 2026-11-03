"""Generate disposable PKI inputs for the public Marketplace sandbox only."""
from pathlib import Path
import subprocess

root = Path('build/marketplace-verification').resolve()
root.mkdir(parents=True, exist_ok=True)

def run(*args):
    subprocess.run(args, cwd=root, check=True, capture_output=True, text=True, timeout=60)

run('openssl', 'req', '-x509', '-newkey', 'rsa:2048', '-nodes', '-sha256', '-days', '30',
    '-keyout', 'root.key', '-out', 'root.pem', '-subj', '/C=AR/O=CertView Demo/CN=Demo Root',
    '-addext', 'basicConstraints=critical,CA:TRUE')
run('openssl', 'req', '-newkey', 'rsa:2048', '-nodes', '-keyout', 'leaf.key', '-out', 'leaf.csr',
    '-subj', '/C=AR/O=CertView Demo/CN=marketplace.example')
(root / 'extensions.txt').write_text('subjectAltName=DNS:marketplace.example\nbasicConstraints=critical,CA:FALSE\n')
run('openssl', 'x509', '-req', '-in', 'leaf.csr', '-CA', 'root.pem', '-CAkey', 'root.key',
    '-CAcreateserial', '-out', 'certificate.pem', '-days', '10', '-sha256', '-extfile', 'extensions.txt')
(root / 'chain.pem').write_bytes((root / 'certificate.pem').read_bytes() + (root / 'root.pem').read_bytes())
run('openssl', 'x509', '-in', 'certificate.pem', '-outform', 'DER', '-out', 'certificate.der')
run('openssl', 'pkcs12', '-export', '-inkey', 'leaf.key', '-in', 'certificate.pem',
    '-certfile', 'root.pem', '-name', 'marketplace-leaf', '-out', 'keystore.p12', '-passout', 'pass:changeit')
for kind in ['JKS', 'JCEKS']:
    output = 'keystore.' + kind.lower()
    (root / output).unlink(missing_ok=True)
    run('keytool', '-importkeystore', '-srckeystore', 'keystore.p12', '-srcstoretype', 'PKCS12',
        '-srcstorepass', 'changeit', '-destkeystore', output, '-deststoretype', kind,
        '-deststorepass', 'changeit', '-destkeypass', 'changeit', '-noprompt')
print('Generated disposable PEM/DER/chain/PKCS12/JKS/JCEKS fixtures; password changeit')
