import os, sys


def run_h1(c, d=30, t=8, host='host-0', api='json', verbose=True):
    t = min(c, t)
    cmd = f'h2load -c {c} -t {t} -D {d} --h1 http://{host}:8080/{api}'
    if verbose:
        print(cmd)
        print(os.popen(cmd).read())
    else:
        os.popen(cmd).read()


if __name__ == '__main__':
    host = sys.argv[1]
    api = sys.argv[2]

    print("Warming up...")
    run_h1(c=64, d=60, host=host, api=api, verbose=False)

    print("Starting benchmarks...")
    for c in [1, 2, 4, 8, 16, 32, 64]:
        run_h1(c=c, host=host, api=api)