"""Narrow, expiring advisory reviews; raw OSV findings are never removed."""
import datetime


def classify(result, policies, today=None):
    today = today or datetime.datetime.now(datetime.timezone.utc).date()
    reviewed = []
    unclassified = []
    entries = {}
    for policy in policies:
        required = ('package', 'version', 'advisory', 'scopes', 'reason', 'evidence', 'issue', 'expires')
        if not isinstance(policy, dict) or not all(policy.get(key) for key in required):
            raise ValueError('Incomplete advisory review')
        if datetime.date.fromisoformat(policy['expires']) < today:
            raise ValueError('Expired advisory review: ' + policy['advisory'])
        key = (policy['package'], policy['version'], policy['advisory'], tuple(sorted(policy['scopes'])))
        if key in entries:
            raise ValueError('Duplicate advisory review')
        entries[key] = policy
    used = set()
    for finding in result['findings']:
        module = finding['module']
        for advisory in finding['advisories']:
            key = (module['group'] + ':' + module['name'], module['version'], advisory['id'], tuple(sorted(module['scopes'])))
            item = {'module': module, 'advisory': advisory}
            if key in entries:
                item['review'] = entries[key]
                reviewed.append(item)
                used.add(key)
            else:
                unclassified.append(item)
    if used != set(entries):
        raise ValueError('Stale advisory review: remove or investigate unmatched entries')
    return result | {'reviewedAdvisories': reviewed, 'unclassifiedAdvisories': unclassified}
