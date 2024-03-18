import { Problem, getProblems } from '@/lib/polybacs-api'
import { useLoaderData, defer, Await } from 'react-router-typesafe'
import { Outlet, NavLink } from 'react-router-dom'
import { Suspense } from 'react'
import { Button } from '@/components/button'
import { UserIcon } from '@heroicons/react/24/solid'

export default function Problems() {
  return (
    <>
      <ProblemList />
      <Outlet />
    </>
  )
}

function loader() {
  return defer({ problems: getProblems() })
}

function ProblemList() {
  const data = useLoaderData<typeof loader>()

  return (
    <Suspense fallback="Loading problems...">
      <Await resolve={data.problems}>
        {(problems) => (
          <div className="flex h-full flex-shrink-0 flex-col gap-1 overflow-y-auto p-1">
            {problems.map((problem) => (
              <ProblemListItem key={problem.id} problem={problem} />
            ))}
          </div>
        )}
      </Await>
    </Suspense>
  )
}

function ProblemListItem({ problem }: { problem: Problem }) {
  return (
    <NavLink to={`/problems/${problem.id}`}>
      {({ isActive }) => (
        <Button
          color={isActive ? 'dark' : 'white'}
          className="flex w-full cursor-pointer flex-col gap-1"
        >
          <div className="flex w-full flex-1 justify-between gap-2">
            <span>{problem.name}</span>
            <span className='flex items-center'><UserIcon className='size-3 mr-1'/>{problem.owner}</span>
          </div>
          <div className="flex w-full justify-between text-sm font-normal">
            <span>{problem.id}</span>
            <span>rev {problem.latestPackage}</span>
          </div>
        </Button>
      )}
    </NavLink>
  )
}

Problems.loader = loader
