"""Split data set into a devevlopment set and a test set with specified ratio."""
import random
import os.path

def split(pairs, dev_ratio):
    """Split pairs such that P(len(dev)/len(pairs)) = dev_ratio."""
    dev, test = [], []
    for p in pairs:
        if random.random() < dev_ratio:
            dev.append(p)
        else:
            test.append(p)

    return dev, test


def write_files(data_dir, prefix, data):
    """Write out a query file along with corresponding gold file."""
    qf = open(os.path.join(data_dir, '%s.queries.txt'%prefix), 'w')
    gf = open(os.path.join(data_dir, '%s.gold.txt'%prefix), 'w')

    for q, g in data:
        qf.write(q)
        gf.write(g)

    qf.close()
    gf.close()


if __name__ == '__main__':
    import sys

    if len(sys.argv) < 3:
        print "Usage: python %s <ratio> <data_dir>" % sys.argv[0]
        print "<ratio> is the ratio between dev and test, e.g. 7:3"
        sys.exit(1)

    ratio_parts = map(int, sys.argv[1].split(':'))
    dev_ratio = 1.0 * ratio_parts[0] / sum(ratio_parts)
    print dev_ratio

    data_dir = sys.argv[2]
    query_file = os.path.join(data_dir, 'queries.txt')
    gold_file = os.path.join(data_dir, 'gold.txt')

    if not os.path.exists(query_file):
        print query_file, "doesn't exist."
        sys.exit(1)

    if not os.path.exists(gold_file):
        print gold_file, "doesn't exist."
        sys.exit(1)

    qf = open(query_file)
    gf = open(gold_file)

    # read-in query-gold pairs
    pairs = zip(qf, gf)

    print len(pairs)
    print pairs[:10]

    qf.close()
    gf.close()

    # split dataset
    dev_set, test_set = split(pairs, dev_ratio)

    # write out
    write_files(data_dir, 'dev', dev_set)
    write_files(data_dir, 'test', test_set)
