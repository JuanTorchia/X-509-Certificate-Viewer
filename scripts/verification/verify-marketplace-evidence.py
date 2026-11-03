"""Fail if the Marketplace test was skipped or IDE log evidence is missing."""
from pathlib import Path
import json
import re
import xml.etree.ElementTree as ET

results = list(Path('build/test-results/integrationTest').glob('TEST-*PublishedMarketplaceSmokeTest.xml'))
assert len(results) == 1, 'Missing exact Marketplace test report'
suite = ET.parse(results[0]).getroot()
assert int(suite.attrib['tests']) == 1 and all(int(suite.attrib.get(k, '0')) == 0 for k in ['failures', 'errors', 'skipped'])
stdout = suite.findtext('system-out', '')
assert re.search(r'MARKETPLACE_PROOF ide=262\.[\w.]+ version=1\.0\.36 source=JetBrainsPluginManager', stdout)
for name in ['certificate.pem', 'certificate.der', 'chain.pem', 'keystore.p12', 'keystore.jks', 'keystore.jceks']:
    assert f'MARKETPLACE_FLOW_PASS file={name} ' in stdout, f'Unverified flow: {name}'
logs = list(Path('out/ide-tests/tests').rglob('idea.log'))
assert logs, 'Missing actual IDE logs; absence is not a clean result'
patterns = ['Plugin to blame: CertView X.509', 'Plugin to blame: X.509 Certificate Viewer',
            '[Plugin: com.architect.certviewer]', 'SEVERE - #com.architect.certviewer']
for log in logs:
    text = log.read_text(errors='replace')
    assert text.strip(), 'Empty IDE log'
    assert not any(p in text for p in patterns), 'IDE log attributes an error to CertView; inspect retained evidence'
receipt = {'publicPlugin': 'com.architect.certviewer', 'version': '1.0.36', 'ideRelease': '2026.2.2',
           'flowsVerified': 6, 'ideaLogsChecked': len(logs), 'testSkipped': False}
Path('build/marketplace-verification-receipt.json').write_text(json.dumps(receipt, indent=2))
print(json.dumps(receipt))
