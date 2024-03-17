import { StrictMode } from 'react'
import ReactDOM from 'react-dom/client'
import { RouterProvider, Navigate } from 'react-router-dom'
import { typesafeBrowserRouter } from 'react-router-typesafe'

import './index.css'
import Root from './routes/root'
import Problems from './routes/problems'
import Problem from './routes/problem'

export const { router } = typesafeBrowserRouter([
  {
    path: '/',
    element: <Root />,
    children: [
      {
        index: true,
        element: <Navigate to="/problems" />,
      },
      {
        path: 'problems',
        element: <Problems />,
        loader: Problems.loader,
        children: [
          {
            path: ':problemId',
            element: <Problem />,
            loader: Problem.loader,
          },
        ],
      },
    ],
  },
])

ReactDOM.createRoot(document.getElementById('app')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
)
