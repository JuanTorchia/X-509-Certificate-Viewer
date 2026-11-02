"""Scan exact resolved Maven coordinates. Errors and partial responses fail closed."""
import json
import pathlib
import sys
import urllib.request


def scan(inventory, query):
    if inventory.get('schemaVersion') != 1 or inventory.get('unresolved'):
        raise ValueError('Invalid or unresolved dependency inventory')
    modules = inventory.get('modules')
    if not isinstance(modules, list) or not modules:
        raise ValueError('Empty Maven inventory')
    findings = []
    for start in range(0, len(modules), 100):
        batch = modules[start:start + 100]
        queries = []
        for module in batch:
            if not all(isinstance(module.get(k), str) and module[k] for k in ('group', 'name', 'version')):
                raise ValueError('Incomplete Maven coordinate')
            queries.append({'package': {'ecosystem': 'Maven', 'name': module['group'] + ':' + module['name']},
                            'version': module['version']})
        response = query({'queries': queries})
        results = response.get('results')
        if not isinstance(results, list) or len(results) != len(batch):
            raise ValueError('Incomplete OSV response')
        for module, result in zip(batch, results):
            if not isinstance(result, dict) or any(key != 'vulns' for key in result):
                raise ValueError('Failed OSV query')
            vulnerabilities = result.get('vulns', [])
            if not isinstance(vulnerabilities, list) or any(not isinstance(v, dict) or not isinstance(v.get('id'), str) or not v['id'] for v in vulnerabilities):
                raise ValueError('Malformed OSV vulnerability result')
            if vulnerabilities:
                findings.append({'module': module, 'advisories': vulnerabilities})
    return {'schemaVersion': 1, 'source': 'https://api.osv.dev/v1/querybatch',
            'queriedCoordinates': len(modules), 'findings': findings,
            'coverageLimits': inventory.get('coverageLimits', []) + [
                'An empty OSV result means no match in this source, not proof that the component has no vulnerability']}


def query_osv(payload):
    request = urllib.request.Request('https://api.osv.dev/v1/querybatch',
                                     data=json.dumps(payload).encode(),
                                     headers={'Content-Type': 'application/json'})
    with urllib.request.urlopen(request, timeout=60) as response:
        return json.load(response)


def main():
    directory = pathlib.Path('build/security')
    directory.mkdir(parents=True, exist_ok=True)
    try:
        result = scan(json.loads((directory / 'inventory.json').read_text()), query_osv)
    except Exception as error:
        (directory / 'osv.json').write_text(json.dumps({'error': str(error), 'scanComplete': False}, indent=2))
        raise
    result['scanComplete'] = True
    (directory / 'osv.json').write_text(json.dumps(result, indent=2) + '\n')
    print(f"OSV queried {result['queriedCoordinates']} Maven coordinates; {len(result['findings'])} affected coordinates")
    return 1 if result['findings'] else 0


if __name__ == '__main__':
    sys.exit(main())
