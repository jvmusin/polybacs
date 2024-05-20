import { z } from 'zod'
import { useEffect, useState } from 'react'
import { toast } from 'react-toastify'

const baseUrl = '/api'

const ProblemSchema = z.object({
  id: z.number(),
  name: z.string(),
  owner: z.string(),
  accessType: z.enum(['READ', 'WRITE', 'OWNER']),
  latestPackage: z.number().nullable(),
})
const ProblemsSchema = z.array(ProblemSchema)
export type Problem = z.infer<typeof ProblemSchema>
export async function getProblems(): Promise<Problem[]> {
  const response = await fetch(`${baseUrl}/problems`)
  const data = await response.json()
  return ProblemsSchema.parse(data)
}

const ProblemInfoSchema = z.object({
  problem: ProblemSchema,
  inputFile: z.string(),
  outputFile: z.string(),
  interactive: z.boolean(),
  timeLimitMillis: z.number(),
  memoryLimitMegabytes: z.number(),
  statementLanguages: z.array(z.string()),
})
export type ProblemInfo = z.infer<typeof ProblemInfoSchema>
export async function getProblemInfo(problemId: number): Promise<ProblemInfo> {
  const response = await fetch(`${baseUrl}/problems/${problemId}`)
  const data = await response.json()
  return ProblemInfoSchema.parse(data)
}

const NameAvailabilitySchema = z.enum(['AVAILABLE', 'TAKEN', 'CHECK_FAILED'])
type NameAvailability = z.infer<typeof NameAvailabilitySchema>
export function useNameAvailability(
  name: string,
): NameAvailability | undefined {
  const [result, setResult] = useState<NameAvailability>()
  useEffect(() => {
    const controller = new AbortController()
    const signal = controller.signal
    const response = fetch(
      `${baseUrl}/problems/nameAvailability?name=${name}`,
      { signal },
    )
    response
      .then(async (response) => {
        setResult(NameAvailabilitySchema.parse(await response.text()))
      })
      .catch((e: Error) => {
        if (e.name === 'AbortError') {
          setResult(undefined)
          return
        }
        console.error(e)
        setResult(NameAvailabilitySchema.Values.CHECK_FAILED)
      })
    return () => {
      setResult(undefined)
      controller.abort()
    }
  }, [name])
  return result
}

type AdditionalProperties = {
  name: string
  prefix: string
  suffix: string
  timeLimitMillis: number
  memoryLimitMegabytes: number
  statementFormat: string
  language: string
}

export function downloadProblem({
  problemId,
  additionalProperties,
}: {
  problemId: number
  additionalProperties: AdditionalProperties
}) {
  fetch(`${baseUrl}/problems/${problemId}/download`, {
    method: 'POST',
    body: JSON.stringify(additionalProperties),
    headers: {
      'Content-Type': 'application/json',
    },
  })
    .then(async (response) => {
      // Check if the response is ok (status in the range 200-299)
      if (!response.ok) {
        throw new Error('Network response was not ok')
      }
      // Extract filename from Content-Disposition header (optional and might not work in all browsers)
      const filename = response.headers
        .get('Content-Disposition')!
        .split('filename=')[1]
        .split(';')[0]
        .replace(/"/g, '')
      // Handle the response as a blob
      const blob = await response.blob()
      // Create a Blob URL
      const url = window.URL.createObjectURL(blob)
      // Create an anchor element and trigger download
      const a = document.createElement('a')
      a.href = url
      // Use the filename from the Content-Disposition header or a default name
      a.download = filename || 'download.zip'
      document.body.appendChild(a) // Append anchor to body
      a.click() // Trigger click on anchor
      document.body.removeChild(a) // Clean up
      window.URL.revokeObjectURL(url) // Release the object URL
    })
    .catch((e) => {
      console.error(e)
      toast.error('Failed to download problem')
    })
}

export function transferProblem({
  problemId,
  additionalProperties,
}: {
  problemId: number
  additionalProperties: AdditionalProperties
}) {
  fetch(`${baseUrl}/problems/${problemId}/transfer`, {
    method: 'POST',
    body: JSON.stringify(additionalProperties),
    headers: {
      'Content-Type': 'application/json',
    },
  }).catch((e) => {
    console.error(e)
    toast.error('Failed to transfer problem')
  })
}
