import unittest
from scan_osv import scan


class CoverageContract(unittest.TestCase):
    def inventory(self):
        return {'schemaVersion': 1, 'modules': [{'group': 'junit', 'name': 'junit', 'version': '4.13.2', 'scopes': ['test']}], 'unresolved': []}

    def test_empty_result_preserves_query_count(self):
        result = scan(self.inventory(), lambda _: {'results': [{}]})
        self.assertEqual(result['queriedCoordinates'], 1)
        self.assertEqual(result['findings'], [])

    def test_findings_keep_coordinate_and_scope(self):
        result = scan(self.inventory(), lambda _: {'results': [{'vulns': [{'id': 'GHSA-example'}]}]})
        self.assertEqual(result['findings'][0]['module']['scopes'], ['test'])

    def test_partial_failed_and_malformed_responses_are_not_clean(self):
        for response in ({}, {'results': []}, {'results': [{'error': 'unavailable'}]}, {'results': [{'vulns': 'bad'}]}):
            with self.subTest(response=response), self.assertRaises(ValueError):
                scan(self.inventory(), lambda _: response)

    def test_empty_and_unresolved_inventory_are_not_clean(self):
        for change in ({'modules': []}, {'unresolved': ['missing dependency']}):
            inventory = self.inventory() | change
            with self.subTest(change=change), self.assertRaises(ValueError):
                scan(inventory, lambda _: {'results': []})


if __name__ == '__main__':
    unittest.main()
