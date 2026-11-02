import datetime
import unittest
from classify_osv import classify


class ReviewBoundary(unittest.TestCase):
    def setUp(self):
        self.today = datetime.date(2026, 11, 2)
        self.result = {'findings': [{'module': {'group': 'example', 'name': 'tool', 'version': '1', 'scopes': ['build-plugin']}, 'advisories': [{'id': 'GHSA-example'}]}]}
        self.policy = {'package': 'example:tool', 'version': '1', 'scopes': ['build-plugin'], 'advisory': 'GHSA-example', 'reason': 'Reviewed build-only route', 'evidence': ['https://example.org/evidence'], 'issue': 'https://example.org/issue', 'expires': '2026-12-02'}

    def test_exact_review_preserves_raw_finding(self):
        result = classify(self.result, [self.policy], self.today)
        self.assertEqual(result['findings'], self.result['findings'])
        self.assertEqual(len(result['reviewedAdvisories']), 1)
        self.assertEqual(result['unclassifiedAdvisories'], [])

    def test_new_advisory_is_blocking(self):
        self.result['findings'][0]['advisories'].append({'id': 'GHSA-new'})
        result = classify(self.result, [self.policy], self.today)
        self.assertEqual(result['unclassifiedAdvisories'][0]['advisory']['id'], 'GHSA-new')

    def test_expired_version_scope_or_incomplete_review_fails(self):
        for change in ({'expires': '2026-11-01'}, {'version': '2'}, {'scopes': ['runtime-candidate']}, {'reason': ''}):
            with self.subTest(change=change), self.assertRaises(ValueError):
                classify(self.result, [self.policy | change], self.today)
