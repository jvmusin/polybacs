import { Button } from '@/components/button'
import { Input } from '@/components/ui/input'
import {
  ProblemInfo,
  downloadProblem,
  getProblemInfo,
  transferProblem as transferProblem,
  useNameAvailability,
} from '@/lib/polybacs-api'
import { zodResolver } from '@hookform/resolvers/zod'
import { Controller, useForm } from 'react-hook-form'
import { z } from 'zod'
import { useLoaderData, defer, Await } from 'react-router-typesafe'
import { LoaderFunctionArgs } from 'react-router'
import { Suspense, useEffect } from 'react'
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Field, FieldGroup, Fieldset, Label } from '@/components/fieldset'
import { Radio, RadioField, RadioGroup } from '@/components/radio'
import { Badge } from '@/components/badge'

function loader({ params }: LoaderFunctionArgs) {
  const problemId = Number(params.problemId)
  const info = getProblemInfo(problemId)
  return defer({ info })
}

const FormSchema = z.object({
  timeLimitMillis: z.coerce.number(),
  memoryLimitMegabytes: z.coerce.number(),
  prefix: z
    .string()
    .refine((v) => !v.includes(' '), { message: 'No spaces allowed' }),
  name: z
    .string()
    .refine((v) => !v.includes(' '), { message: 'No spaces allowed' }),
  suffix: z
    .string()
    .refine((v) => !v.includes(' '), { message: 'No spaces allowed' }),
  statement: z.enum(['HTML', 'PDF']),
})

type FormType = ReturnType<typeof useForm<z.infer<typeof FormSchema>>>

function LimitsCard({ form }: { form: FormType }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">Limits</CardTitle>
      </CardHeader>
      <CardContent>
        <FieldGroup>
          <div className="space-y-4">
            <Field>
              <Label>Time Limit in milliseconds</Label>
              <Input {...form.register('timeLimitMillis')} type="number" />
            </Field>
            <Field>
              <Label>Memory Limit in megabytes</Label>
              <Input {...form.register('memoryLimitMegabytes')} type="number" />
            </Field>
          </div>
        </FieldGroup>
      </CardContent>
    </Card>
  )
}

function NameAvailabilityBadge({ name }: { name: string }) {
  const available = useNameAvailability(name)
  return (
    <div className="flex justify-center space-x-2 whitespace-pre font-mono text-lg font-bold">
      <code>{name}</code>
      {available === undefined ? (
        <Badge color="zinc">
          <span className="font-bold">checking...</span>
        </Badge>
      ) : available === 'AVAILABLE' ? (
        <Badge color="lime">
          <span className="font-bold">available</span>
        </Badge>
      ) : available === 'TAKEN' ? (
        <Badge color="red">
          <span className="font-bold">taken</span>
        </Badge>
      ) : available === 'CHECK_FAILED' ? (
        <Badge color="red">
          <span className="font-bold">check failed</span>
        </Badge>
      ) : null}
    </div>
  )
}

function NameModifiersCard({ form }: { form: FormType }) {
  const finalName = `${form.watch('prefix')}${form.watch('name')}${form.watch('suffix')}`
  return (
    <Card className="col-span-2">
      <CardHeader>
        <CardTitle className="text-xl">Name Modifiers</CardTitle>
      </CardHeader>
      <CardContent className="space-y-5">
        <Fieldset>
          <div className="grid grid-cols-3 gap-x-3">
            <Field>
              <Label>Prefix</Label>
              <Input {...form.register('prefix')} type="text" />
            </Field>
            <Field>
              <Label>Name</Label>
              <Input {...form.register('name')} type="text" disabled />
            </Field>
            <Field>
              <Label>Suffix</Label>
              <Input {...form.register('suffix')} type="text" />
            </Field>
          </div>
        </Fieldset>
        <NameAvailabilityBadge name={finalName} />
      </CardContent>
    </Card>
  )
}

function MiscCard({ form }: { form: FormType }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">Misc</CardTitle>
      </CardHeader>
      <CardContent>
        <Controller
          name="statement"
          control={form.control}
          render={({ field: { onChange, value, onBlur } }) => (
            <RadioGroup
              className="flex flex-col space-y-0"
              onChange={onChange}
              value={value}
              onBlur={onBlur}
            >
              <div className="space-y-2">
                <RadioField className="flex items-center space-x-2 space-y-0">
                  <Radio value="HTML">HTML</Radio>
                  <Label className="font-normal">HTML</Label>
                </RadioField>
                <RadioField className="flex items-center space-x-2 space-y-0">
                  <Radio value="PDF">PDF</Radio>
                  <Label className="font-normal">PDF</Label>
                </RadioField>
              </div>
            </RadioGroup>
          )}
        />
      </CardContent>
    </Card>
  )
}

function useProblemForm(info: ProblemInfo): FormType {
  return useForm<z.infer<typeof FormSchema>>({
    resolver: zodResolver(FormSchema),
    defaultValues: {
      timeLimitMillis: info.timeLimitMillis,
      memoryLimitMegabytes: info.memoryLimitMegabytes,
      prefix: 'polybacs-',
      name: info.problem.name,
      suffix: '',
      statement: 'HTML',
    },
  })
}

function Header({ info }: { info: ProblemInfo }) {
  return (
    <CardHeader className="text-center">
      <CardTitle className="text-2xl">{info.problem.name}</CardTitle>
      <div className="flex items-center justify-center text-gray-600">
        <CardDescription>{info.problem.owner}</CardDescription>
        <span className="mx-2 size-2 select-none rounded-full bg-gray-300" />
        <CardDescription>
          {`${info.inputFile}/${info.outputFile}`}
        </CardDescription>
      </div>
    </CardHeader>
  )
}

function Main({ form }: { form: FormType }) {
  return (
    <CardContent>
      <div className="w-100 grid grid-cols-2 gap-3 text-sm">
        <LimitsCard form={form} />
        <MiscCard form={form} />
        <NameModifiersCard form={form} />
      </div>
    </CardContent>
  )
}

function Footer({ info, form }: { info: ProblemInfo; form: FormType }) {
  function extract(data: z.infer<typeof FormSchema>) {
    return {
      problemId: info.problem.id,
      additionalProperties: {
        prefix: data.prefix,
        suffix: data.suffix,
        timeLimitMillis: data.timeLimitMillis,
        memoryLimitMegabytes: data.memoryLimitMegabytes,
        statementFormat: data.statement,
      },
    }
  }
  function handleDownloadProblem(data: z.infer<typeof FormSchema>) {
    downloadProblem(extract(data))
  }
  function handleTransferProblem(data: z.infer<typeof FormSchema>) {
    transferProblem(extract(data))
  }
  function ActionButton({
    action,
    name,
  }: {
    action: (data: z.infer<typeof FormSchema>) => void
    name: string
  }) {
    return (
      <Button
        type="button"
        className="cursor-pointer"
        onClick={form.handleSubmit(action)}
      >
        {name}
      </Button>
    )
  }
  return (
    <CardFooter className="flex justify-end space-x-3">
      <ActionButton action={handleDownloadProblem} name="Download" />
      <ActionButton action={handleTransferProblem} name="Transfer" />
    </CardFooter>
  )
}

function LoadedProblem({ info }: { info: ProblemInfo }) {
  const form = useProblemForm(info)
  useEffect(() => {
    form.reset()
  }, [info, form])
  return (
    <form className="flex-grow">
      <Fieldset>
        <Card>
          <Header info={info} />
          <Main form={form} />
          <Footer info={info} form={form} />
        </Card>
      </Fieldset>
    </form>
  )
}

export default function Problem() {
  const data = useLoaderData<typeof loader>()

  return (
    <Suspense fallback="Loading problem...">
      <Await resolve={data.info}>
        {(info) => <LoadedProblem info={info} />}
      </Await>
    </Suspense>
  )
}

Problem.loader = loader